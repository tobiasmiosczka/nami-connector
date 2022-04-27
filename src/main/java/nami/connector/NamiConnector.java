package nami.connector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import nami.connector.exception.NamiException;
import nami.connector.exception.NamiLoginException;
import nami.connector.namitypes.*;

public class NamiConnector {

    private final NamiHttpClient httpClient;
    private final NamiServer server;
    private final NamiUriFactory uriFactory;

    private static final int INITIAL_LIMIT = 1000; // Maximale Anzahl der gefundenen Datensätze, wenn kein Limit vorgegeben wird.

    public NamiConnector(NamiServer server) {
        this.server = server;
        this.httpClient = new NamiHttpClient();
        this.uriFactory = new NamiUriFactory(server);
    }

    public void login(String username, String password) throws IOException, NamiLoginException, InterruptedException {
        httpClient.login(server, username, password);
    }

    // TODO: Warum NamiResponse nötig
    // -> gebe stattdessen direkt die Collection zurück oder null, wenn kein
    // success
    public NamiResponse<Collection<NamiMitglied>> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start) throws IOException, NamiException, InterruptedException {
        URI uri = uriFactory.namiSearch(limit, page, start, searchedValues);
        return httpClient.executeApiRequest(
                buildGetRequest(uri),
                new TypeToken<NamiResponse<Collection<NamiMitglied>>>() {}.getType());
    }

    // TODO: Teste was passiert, wenn es keine Treffer gibt bzw. die Suchanfrage ungültig ist
    public Collection<NamiMitglied> getAllResults(NamiSearchedValues searchedValues) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiMitglied>> resp = getSearchResult(searchedValues, INITIAL_LIMIT, 1, 0);
        if (resp.getTotalEntries() > INITIAL_LIMIT)
            resp = getSearchResult(searchedValues, resp.getTotalEntries(), 1, 0);
        return resp.getData();
    }

    public NamiMitglied getMitgliedById(int id) throws IOException, NamiException, InterruptedException {
        NamiResponse<NamiMitglied> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.namiMitglieder(id)),
                new TypeToken<NamiResponse<NamiMitglied>>() {}.getType());
        return (resp.isSuccess() ? resp.getData() : null);
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(NamiMitglied namiMitglied) throws IOException, NamiException, InterruptedException {
        return getSchulungen(namiMitglied.getId());
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(int mitgliedsID) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiSchulung>> response = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.namiSchulungen(mitgliedsID)),
                new TypeToken<NamiResponse<Collection<NamiSchulung>>>() {}.getType());
        return response.getData().stream()
                .collect(Collectors.toMap(NamiSchulung::getBaustein, Function.identity()));
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException, IOException, InterruptedException {
        NamiResponse<List<NamiEnum>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten()),
                new TypeToken<NamiResponse<List<NamiEnum>>>() {}.getType());
        return resp.getData();
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException, IOException, InterruptedException {
        NamiResponse<List<NamiEnum>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.namiUntergliederungen()),
                new TypeToken<NamiResponse<List<NamiEnum>>>() {}.getType());
        return resp.getData();
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException, IOException, InterruptedException {
        NamiResponse<Collection<NamiMitglied>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.memberFromGroup(gruppierungsnummer)),
                new TypeToken<NamiResponse<Collection<NamiMitglied>>>() {}.getType());
        if (resp.isSuccess())
            return resp.getData();
        else
            throw new NamiException("Could not get member list from Nami: " + resp.getMessage());
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiTaetigkeitAssignment>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.namiTaetigkeiten(id)),
                new TypeToken<NamiResponse<Collection<NamiTaetigkeitAssignment>>>() {}.getType());
        if (resp.isSuccess())
            return resp.getData();
        else
            return null;
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiGruppierung>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.childGroups(rootGruppierung)),
                new TypeToken<NamiResponse<Collection<NamiGruppierung>>>() {}.getType());
        Collection<NamiGruppierung> allChildren = resp.getData();
        Collection<NamiGruppierung> activeChildren = new LinkedList<>();
        for (NamiGruppierung child : allChildren) {
            activeChildren.add(child);
            if (child.getEbene() == NamiEbene.STAMM)
                child.setChildren(new LinkedList<>());
            else
                child.setChildren(getChildGruppierungen(child.getId()));
        }
        return activeChildren;
    }

    public NamiGruppierung getRootGruppierung() throws IOException, NamiException, InterruptedException {
        NamiGruppierung rootGrp = getRootGruppierungWithoutChildren();
        rootGrp.setChildren(getChildGruppierungen(rootGrp.getId()));
        return rootGrp;
    }

    public Collection<NamiGruppierung> getGruppierungenFromUser() throws IOException, NamiException, InterruptedException {
        return getGruppierungenFromUser(-1);
    }

    private Collection<NamiGruppierung> getGruppierungenFromUser(int id) throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiGruppierung>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.groupsByUser(id)),
                new TypeToken<NamiResponse<Collection<NamiGruppierung>>>() {}.getType());
        Collection<NamiGruppierung> results = resp.getData();
        Collection<NamiGruppierung> newResults = new LinkedList<>();
        for (NamiGruppierung namiGruppierung : results)
            newResults.addAll(getGruppierungenFromUser(namiGruppierung.getId()));
        results.addAll(newResults);
        return results;
    }

    public NamiGruppierung getGruppierung(int gruppierungsnummer) throws IOException, NamiException, InterruptedException {
        // nicht sehr effizient, da trotzdem der gesamte Baum aus NaMi geladen
        // wird
        // auf Diözesanebene sollte das aber kein Problem sein, da die Anzahl
        // der Bezirke doch sehr begrenzt ist
        NamiGruppierung found = getRootGruppierung().findGruppierung(gruppierungsnummer);
        if (found == null)
            throw new NamiException("Gruppierung not found: " + gruppierungsnummer);
        else
            return found;
    }

    private NamiGruppierung getRootGruppierungWithoutChildren() throws IOException, NamiException, InterruptedException {
        NamiResponse<Collection<NamiGruppierung>> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.rootGroupWithoutChildren()),
                new TypeToken<NamiResponse<Collection<NamiGruppierung>>>() {}.getType());
        if (!resp.isSuccess())
            throw new NamiException("Could not get root Gruppierung");
        NamiGruppierung rootGrp = resp.getData().iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    public NamiTaetigkeitAssignment getTaetigkeit(int personId, int taetigkeitId) throws IOException, InterruptedException, NamiException {
        NamiResponse<NamiTaetigkeitAssignment> resp = httpClient.executeApiRequest(
                buildGetRequest(uriFactory.taetigkeitByPersonIdAndTeatigkeitId(personId, taetigkeitId)),
                new TypeToken<NamiResponse<NamiTaetigkeitAssignment>>() {}.getType());
        if (resp.isSuccess()) {
            return resp.getData();
        } else {
            return null;
        }
    }

    private static HttpRequest buildGetRequest(URI uri) {
        return HttpRequest.newBuilder().uri(uri).GET().build();
    }
}
