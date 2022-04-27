package nami.connector;

import java.io.IOException;
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
import nami.connector.json.JsonUtil;
import nami.connector.namitypes.*;

public class NamiConnector {

    private final NamiHttpClient httpClient;
    private final NamiServer server;

    private static final int INITIAL_LIMIT = 1000; // Maximale Anzahl der gefundenen Datensätze, wenn kein Limit vorgegeben wird.
    private static final int MAX_TAETIGKEITEN = 1000;

    public NamiConnector(NamiServer server) {
        this.server = server;
        this.httpClient = new NamiHttpClient();
    }

    public void login(String username, String password) throws IOException, NamiLoginException, InterruptedException {
        httpClient.login(server, username, password);
    }

    public UriBuilder getURIBuilder(String path) {
        return getURIBuilder(path, true);
    }

    public UriBuilder getURIBuilder(String path, boolean restUrl) {
        return new NamiUriBuilder(server, path, restUrl);
    }

    // TODO: Warum NamiResponse nötig
    // -> gebe stattdessen direkt die Collection zurück oder null, wenn kein
    // success
    public NamiResponse<Collection<NamiMitglied>> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_NAMI_SEARCH)
                .setParameter("limit", limit)
                .setParameter("page", page)
                .setParameter("start", start)
                .setParameter("searchedValues", JsonUtil.toJson(searchedValues));
        return httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
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
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_NAMI_MITGLIEDER)
                .appendPath(0)
                .appendPath(id);
        NamiResponse<NamiMitglied> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<NamiMitglied>>() {}.getType());
        return (resp.isSuccess() ? resp.getData() : null);
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(NamiMitglied namiMitglied) throws IOException, NamiException, InterruptedException {
        return getSchulungen(namiMitglied.getId());
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(int mitgliedsID) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_SCHULUNGEN)
                .appendPath(mitgliedsID)
                .appendPath("/flist");
        NamiResponse<Collection<NamiSchulung>> response = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiSchulung>>>() {}.getType());
        return response.getData().stream()
                .collect(Collectors.toMap(NamiSchulung::getBaustein, Function.identity()));
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException, IOException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_TAETIGKEITEN);
        NamiResponse<List<NamiEnum>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<List<NamiEnum>>>() {}.getType());
        return resp.getData();
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException, IOException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_UNTERGLIEDERUNGEN);
        NamiResponse<List<NamiEnum>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<List<NamiEnum>>>() {}.getType());
        return resp.getData();
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException, IOException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_NAMI_MITGLIEDER);
        builder.appendPath(gruppierungsnummer)
                .appendPath("flist")
                .setParameter("limit", 5000)
                .setParameter("page", 1)
                .setParameter("start", 0);
        NamiResponse<Collection<NamiMitglied>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiMitglied>>>() {}.getType());
        if (resp.isSuccess())
            return resp.getData();
        else
            throw new NamiException("Could not get member list from Nami: " + resp.getMessage());
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_NAMI_TAETIGKEIT)
                .appendPath(id)
                .appendPath("flist")
                .setParameter("limit", MAX_TAETIGKEITEN)
                .setParameter("page", 0)
                .setParameter("start", 0);
        NamiResponse<Collection<NamiTaetigkeitAssignment>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiTaetigkeitAssignment>>>() {}.getType());
        if (resp.isSuccess())
            return resp.getData();
        else
            return null;
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_GRUPPIERUNGEN)
                .appendPath(rootGruppierung)
                .setParameter("node", rootGruppierung);
        NamiResponse<Collection<NamiGruppierung>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
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
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_GRUPPIERUNGEN);
        if (id != -1)
            builder.appendPath(id);
        NamiResponse<Collection<NamiGruppierung>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiGruppierung>>>() {}.getType());
        Collection<NamiGruppierung> results = resp.getData();
        Collection<NamiGruppierung> newResults = new LinkedList<>();
        for (NamiGruppierung namiGruppierung : results)
            newResults.addAll(getGruppierungenFromUser(namiGruppierung.getId()));
        results.addAll(newResults);
        return results;
    }

    public NamiGruppierung getGruppierung(int gruppierungsnummer) throws IOException, NamiException, InterruptedException {
        NamiGruppierung rootGrp = getRootGruppierung();
        // nicht sehr effizient, da trotzdem der gesamte Baum aus NaMi geladen
        // wird
        // auf Diözesanebene sollte das aber kein Problem sein, da die Anzahl
        // der Bezirke doch sehr begrenzt ist
        NamiGruppierung found = rootGrp.findGruppierung(gruppierungsnummer);
        if (found == null)
            throw new NamiException("Gruppierung not found: " + gruppierungsnummer);
        else
            return found;
    }

    private NamiGruppierung getRootGruppierungWithoutChildren() throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_GRUPPIERUNGEN)
                .appendPath("root")
                .setParameter("node", "root");
        NamiResponse<Collection<NamiGruppierung>> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiGruppierung>>>() {}.getType());
        if (!resp.isSuccess())
            throw new NamiException("Could not get root Gruppierung");
        NamiGruppierung rootGrp = resp.getData().iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    public NamiTaetigkeitAssignment getTaetigkeit(int personId, int taetigkeitId) throws IOException, InterruptedException, NamiException {
        UriBuilder builder = getURIBuilder(NamiUriBuilder.URL_NAMI_TAETIGKEIT)
                .appendPath(personId)
                .appendPath(taetigkeitId);
        NamiResponse<NamiTaetigkeitAssignment> resp = httpClient.executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<NamiTaetigkeitAssignment>>() {}.getType());
        if (resp.isSuccess()) {
            return resp.getData();
        } else {
            return null;
        }
    }
}
