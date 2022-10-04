package nami.connector;

import java.util.*;

import nami.connector.exception.NamiException;
import nami.connector.httpclient.NamiHttpClient;
import nami.connector.httpclient.impl.NativeJava11NamiHttpClient;
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

public class NamiConnector {

    private final NamiHttpClient httpClient;
    private final NamiServer server;
    private final NamiUriFactory uriFactory;

    private static final int INITIAL_LIMIT = 1000;

    public NamiConnector(NamiServer server) {
        this.server = server;
        this.httpClient = new NativeJava11NamiHttpClient();
        this.uriFactory = new NamiUriFactory(server);
    }

    public void login(String username, String password) throws NamiException {
        httpClient.login(server, username, password);
    }

    public Collection<NamiMitglied> getAllResults(NamiSearchedValues searchedValues) throws NamiException {
        return getSearchResult(searchedValues, INITIAL_LIMIT, 1, 0);
    }

    public NamiMitglied getMitgliedById(int id) throws NamiException {
        return this.httpClient.getSingle(uriFactory.namiMitglieder(id), NamiMitglied.class);
    }

    public Collection<NamiSchulung> getTrainingsByUser(int userId) throws NamiException {
        return httpClient.getList(uriFactory.namiSchulungen(userId), NamiSchulung.class);
    }

    public Map<NamiBaustein, NamiSchulung> getLatestTrainingsByUser(int userId) throws NamiException {
        Collection<NamiSchulung> result = getTrainingsByUser(userId);
        return reduceToLatest(result);
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException {
        return httpClient.getList(uriFactory.namiTaetigkeiten(), NamiEnum.class);
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException {
        return httpClient.getList(uriFactory.namiUntergliederungen(), NamiEnum.class);
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException {
        return httpClient.getList(uriFactory.memberFromGroup(gruppierungsnummer), NamiMitglied.class);
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws NamiException {
        return httpClient.getList(uriFactory.namiTaetigkeiten(id), NamiTaetigkeitAssignment.class);
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws NamiException {
        List<NamiGruppierung> allChildren = httpClient.getList(uriFactory.childGroups(rootGruppierung), NamiGruppierung.class);
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
        List<NamiGruppierung> results = httpClient.getList(uriFactory.groupsByUser(id), NamiGruppierung.class);
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
        return httpClient.getSingle(uriFactory.taetigkeitByPersonIdAndTeatigkeitId(personId, taetigkeitId), NamiTaetigkeitAssignment.class);
    }

    private NamiGruppierung getRootGruppierungWithoutChildren() throws NamiException {
        List<NamiGruppierung> response = httpClient.getList(uriFactory.rootGroupWithoutChildren(), NamiGruppierung.class);
        NamiGruppierung rootGrp = response.iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    private Collection<NamiMitglied> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start) throws NamiException {
        return httpClient.getList(uriFactory.namiSearch(limit, page, start, searchedValues), NamiMitglied.class);
    }
}
