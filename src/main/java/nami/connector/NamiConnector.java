package nami.connector;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import nami.connector.exception.NamiException;
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

import static nami.connector.NamiUtil.reduceToLatest;
import static nami.connector.httpclient.HttpUtil.buildGetRequest;

public class NamiConnector {

    private final NamiHttpClient httpClient;
    private final NamiServer server;
    private final NamiUriFactory uriFactory;

    private static final int INITIAL_LIMIT = 1000;

    public NamiConnector(NamiServer server) {
        this.server = server;
        this.httpClient = new NativeJavaNamiHttpClient();
        this.uriFactory = new NamiUriFactory(server);
    }

    public void login(String username, String password) throws NamiException {
        httpClient.login(server, username, password);
    }

    public Collection<NamiMitglied> getAllResults(NamiSearchedValues searchedValues) throws NamiException {
        return getSearchResult(searchedValues, INITIAL_LIMIT, 1, 0);
    }

    public Optional<NamiMitglied> getMitgliedById(int id) throws NamiException {
        NamiMitglied response = this.executeApiRequest(buildGetRequest(uriFactory.namiMitglieder(id)), new TypeReference<NamiMitglied>() {}.getType());
        return Optional.ofNullable(response);
    }

    public Collection<NamiSchulung> getTrainingsByUser(int userId) throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiSchulungen(userId)),
                new TypeReference<Collection<NamiSchulung>>() {}.getType());
    }

    public Map<NamiBaustein, NamiSchulung> getLatestTrainingsByUser(int userId) throws NamiException {
        Collection<NamiSchulung> result = getTrainingsByUser(userId);
        return reduceToLatest(result);
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten()),
                new TypeReference<List<NamiEnum>>() {}.getType());
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiUntergliederungen()),
                new TypeReference<List<NamiEnum>>() {}.getType());
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.memberFromGroup(gruppierungsnummer)),
                new TypeReference<Collection<NamiMitglied>>() {}.getType());
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten(id)),
                new TypeReference<Collection<NamiTaetigkeitAssignment>>() {}.getType());
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws NamiException {
        Collection<NamiGruppierung> allChildren = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.childGroups(rootGruppierung)),
                new TypeReference<Collection<NamiGruppierung>>() {}.getType());
        Collection<NamiGruppierung> activeChildren = new LinkedList<>();
        for (NamiGruppierung child : allChildren) {
            activeChildren.add(child);
            child.setChildren(child.getEbene() == NamiEbene.STAMM ? new LinkedList<>() : getChildGruppierungen(child.getId()));
        }
        return activeChildren;
    }

    public NamiGruppierung getRootGruppierung() throws NamiException {
        NamiGruppierung rootGroup = getRootGruppierungWithoutChildren();
        rootGroup.setChildren(getChildGruppierungen(rootGroup.getId()));
        return rootGroup;
    }

    public Collection<NamiGruppierung> getGruppierungenFromUser() throws NamiException {
        return getGruppierungenFromUser(-1);
    }

    public Collection<NamiGruppierung> getGruppierungenFromUser(int id) throws NamiException {
        Collection<NamiGruppierung> results = this.executeApiRequest(
                buildGetRequest(uriFactory.groupsByUser(id)),
                new TypeReference<Collection<NamiGruppierung>>() {}.getType());
        Collection<NamiGruppierung> newResults = new LinkedList<>();
        for (NamiGruppierung namiGruppierung : results)
            newResults.addAll(getGruppierungenFromUser(namiGruppierung.getId()));
        results.addAll(newResults);
        return results;
    }

    public Optional<NamiGruppierung> getGruppierung(int groupNumber) throws NamiException {
        return getRootGruppierung().findGruppierung(groupNumber);
    }

    public NamiTaetigkeitAssignment getTaetigkeit(int personId, int taetigkeitId) throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.taetigkeitByPersonIdAndTeatigkeitId(personId, taetigkeitId)),
                new TypeReference<NamiTaetigkeitAssignment>() {}.getType());
    }

    private NamiGruppierung getRootGruppierungWithoutChildren() throws NamiException {
        Collection<NamiGruppierung> response = this.executeApiRequest(
                buildGetRequest(uriFactory.rootGroupWithoutChildren()),
                new TypeReference<Collection<NamiGruppierung>>() {}.getType());
        NamiGruppierung rootGrp = response.iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    private Collection<NamiMitglied> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start) throws NamiException {
        return this.executeApiRequest(
                buildGetRequest(uriFactory.namiSearch(limit, page, start, searchedValues)),
                new TypeReference<Collection<NamiMitglied>>() {}.getType());
    }

    private <T> T executeApiRequest(HttpRequest request, Type type) throws NamiException {
        return httpClient.executeApiRequest(request, type);
    }
}
