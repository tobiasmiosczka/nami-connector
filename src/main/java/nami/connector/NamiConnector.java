package nami.connector;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;
import nami.connector.exception.NamiException;
import nami.connector.exception.NamiLoginException;
import nami.connector.httpclient.NamiHttpClient;
import nami.connector.httpclient.impl.NativeJavaNamiHttpClient;
import nami.connector.namitypes.NamiBaustein;
import nami.connector.namitypes.NamiEbene;
import nami.connector.namitypes.NamiEnum;
import nami.connector.namitypes.NamiGruppierung;
import nami.connector.namitypes.NamiMitglied;
import nami.connector.namitypes.NamiSchulung;
import nami.connector.namitypes.NamiSearchedValues;
import nami.connector.namitypes.NamiTaetigkeitAssignment;
import nami.connector.uri.NamiUriFactory;

import static nami.connector.httpclient.HttpUtil.buildGetRequest;

public class NamiConnector {

    private final NamiHttpClient httpClient;
    private final NamiServer server;
    private final NamiUriFactory uriFactory;

    private static final int INITIAL_LIMIT = 1000; // Maximale Anzahl der gefundenen Datensätze, wenn kein Limit vorgegeben wird.

    public NamiConnector(NamiServer server) {
        this.server = server;
        this.httpClient = new NativeJavaNamiHttpClient();
        this.uriFactory = new NamiUriFactory(server);
    }

    public void login(String username, String password) throws IOException, NamiLoginException, InterruptedException {
        httpClient.login(server, username, password);
    }

    public Collection<NamiMitglied> getAllResults(NamiSearchedValues searchedValues) throws IOException, NamiException, InterruptedException {
        return getSearchResult(searchedValues, INITIAL_LIMIT, 1, 0);
    }

    public Optional<NamiMitglied> getMitgliedById(int id) throws IOException, NamiException, InterruptedException {
        NamiMitglied response = this.executeApiRequest(buildGetRequest(uriFactory.namiMitglieder(id)), new TypeToken<NamiMitglied>() {}.getType());
        return Optional.ofNullable(response);
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(int userId) throws IOException, NamiException, InterruptedException {
        return this.<Collection<NamiSchulung>>executeApiRequest(
                buildGetRequest(uriFactory.namiSchulungen(userId)),
                new TypeToken<Collection<NamiSchulung>>() {}.getType()).stream()
                .collect(Collectors.toMap(NamiSchulung::getBaustein, Function.identity()));
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException, IOException, InterruptedException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten()),
                new TypeToken<List<NamiEnum>>() {}.getType());
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException, IOException, InterruptedException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiUntergliederungen()),
                new TypeToken<List<NamiEnum>>() {}.getType());
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException, IOException, InterruptedException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.memberFromGroup(gruppierungsnummer)),
                new TypeToken<Collection<NamiMitglied>>() {}.getType());
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws IOException, NamiException, InterruptedException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten(id)),
                new TypeToken<Collection<NamiTaetigkeitAssignment>>() {}.getType());
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws IOException, NamiException, InterruptedException {
        Collection<NamiGruppierung> allChildren = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.childGroups(rootGruppierung)),
                new TypeToken<Collection<NamiGruppierung>>() {}.getType());
        Collection<NamiGruppierung> activeChildren = new LinkedList<>();
        for (NamiGruppierung child : allChildren) {
            activeChildren.add(child);
            child.setChildren(child.getEbene() == NamiEbene.STAMM ? new LinkedList<>() : getChildGruppierungen(child.getId()));
        }
        return activeChildren;
    }

    public NamiGruppierung getRootGruppierung() throws IOException, NamiException, InterruptedException {
        NamiGruppierung rootGroup = getRootGruppierungWithoutChildren();
        rootGroup.setChildren(getChildGruppierungen(rootGroup.getId()));
        return rootGroup;
    }

    public Collection<NamiGruppierung> getGruppierungenFromUser() throws IOException, NamiException, InterruptedException {
        return getGruppierungenFromUser(-1);
    }

    public Collection<NamiGruppierung> getGruppierungenFromUser(int id) throws IOException, NamiException, InterruptedException {
        Collection<NamiGruppierung> results = this.executeApiRequest(
                buildGetRequest(uriFactory.groupsByUser(id)),
                new TypeToken<Collection<NamiGruppierung>>() {}.getType());
        Collection<NamiGruppierung> newResults = new LinkedList<>();
        for (NamiGruppierung namiGruppierung : results)
            newResults.addAll(getGruppierungenFromUser(namiGruppierung.getId()));
        results.addAll(newResults);
        return results;
    }

    public Optional<NamiGruppierung> getGruppierung(int groupNumber) throws IOException, NamiException, InterruptedException {
        return getRootGruppierung().findGruppierung(groupNumber);
    }

    public NamiTaetigkeitAssignment getTaetigkeit(int personId, int taetigkeitId) throws IOException, InterruptedException, NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.taetigkeitByPersonIdAndTeatigkeitId(personId, taetigkeitId)),
                new TypeToken<NamiTaetigkeitAssignment>() {}.getType());
    }

    private NamiGruppierung getRootGruppierungWithoutChildren() throws IOException, NamiException, InterruptedException {
        Collection<NamiGruppierung> response = this.executeApiRequest(
                buildGetRequest(uriFactory.rootGroupWithoutChildren()),
                new TypeToken<Collection<NamiGruppierung>>() {}.getType());
        NamiGruppierung rootGrp = response.iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    private Collection<NamiMitglied> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start)
            throws IOException, NamiException, InterruptedException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiSearch(limit, page, start, searchedValues)),
                new TypeToken<Collection<NamiMitglied>>() {}.getType());
    }

    private <T> T executeApiRequest(HttpRequest request, Type type) throws IOException, NamiException, InterruptedException {
        return httpClient.executeApiRequest(request, type);
    }
}
