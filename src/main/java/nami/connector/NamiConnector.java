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
import nami.connector.namitypes.*;
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

    // TODO: Teste was passiert, wenn es keine Treffer gibt bzw. die Suchanfrage ungültig ist
    public Collection<NamiMitglied> getAllResults(NamiSearchedValues searchedValues) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiMitglied>> response = getSearchResult(searchedValues, INITIAL_LIMIT, 1, 0);
        if (response.getTotalEntries() > INITIAL_LIMIT)
            response = getSearchResult(searchedValues, response.getTotalEntries(), 1, 0);
        return response.getData();
    }

    public Optional<NamiMitglied> getMitgliedById(int id) throws IOException, NamiException, InterruptedException {
        NamiResponse<NamiMitglied> response = this.executeApiRequest(
                buildGetRequest(uriFactory.namiMitglieder(id)),
                new TypeToken<NamiMitglied>() {}.getType());
        return (response.isSuccess() ? Optional.ofNullable(response.getData()) : Optional.empty());
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(int userId) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiSchulung>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.namiSchulungen(userId)),
                new TypeToken<Collection<NamiSchulung>>() {}.getType());
        return response.getData().stream()
                .collect(Collectors.toMap(NamiSchulung::getBaustein, Function.identity()));
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException, IOException, InterruptedException {
        NamiResponse<List<NamiEnum>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten()),
                new TypeToken<List<NamiEnum>>() {}.getType());
        return response.getData();
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException, IOException, InterruptedException {
        NamiResponse<List<NamiEnum>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.namiUntergliederungen()),
                new TypeToken<List<NamiEnum>>() {}.getType());
        return response.getData();
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException, IOException, InterruptedException {
        NamiResponse<Collection<NamiMitglied>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.memberFromGroup(gruppierungsnummer)),
                new TypeToken<Collection<NamiMitglied>>() {}.getType());
        if (!response.isSuccess())
            throw new NamiException("Could not get member list from Nami: " + response.getMessage());
        return response.getData();
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiTaetigkeitAssignment>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten(id)),
                new TypeToken<Collection<NamiTaetigkeitAssignment>>() {}.getType());
        return (response.isSuccess() ? response.getData() : null);
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws IOException, NamiException, InterruptedException {
        Collection<NamiGruppierung> allChildren = httpClient.<Collection<NamiGruppierung>>executeApiRequest(
                buildGetRequest(uriFactory.childGroups(rootGruppierung)),
                new TypeToken<Collection<NamiGruppierung>>() {}.getType()).getData();
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
        NamiResponse<Collection<NamiGruppierung>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.groupsByUser(id)),
                new TypeToken<Collection<NamiGruppierung>>() {}.getType());
        Collection<NamiGruppierung> results = response.getData();
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
        NamiResponse<NamiTaetigkeitAssignment> response = this.executeApiRequest(
                buildGetRequest(uriFactory.taetigkeitByPersonIdAndTeatigkeitId(personId, taetigkeitId)),
                new TypeToken<NamiTaetigkeitAssignment>() {}.getType());
        return (response.isSuccess() ? response.getData() : null);
    }

    private NamiGruppierung getRootGruppierungWithoutChildren() throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiGruppierung>> response = this.executeApiRequest(
                buildGetRequest(uriFactory.rootGroupWithoutChildren()),
                new TypeToken<Collection<NamiGruppierung>>() {}.getType());
        if (!response.isSuccess())
            throw new NamiException("Could not get root Gruppierung");
        NamiGruppierung rootGrp = response.getData().iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    // TODO: Warum NamiResponse nötig
    // -> gebe stattdessen direkt die Collection zurück oder null, wenn kein
    // success
    private NamiResponse<Collection<NamiMitglied>> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start)
            throws IOException, NamiException, InterruptedException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiSearch(limit, page, start, searchedValues)),
                new TypeToken<Collection<NamiMitglied>>() {}.getType());
    }

    private <T> NamiResponse<T> executeApiRequest(HttpRequest request, Type type) throws IOException, NamiException, InterruptedException {
        return httpClient.executeApiRequest(request, type);
    }
}
