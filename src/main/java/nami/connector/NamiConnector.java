package nami.connector;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import nami.connector.exception.NamiException;
import nami.connector.exception.NamiLoginException;
import nami.connector.json.JsonUtil;
import nami.connector.namitypes.*;

public class NamiConnector {

    private static final Logger log = Logger.getLogger(NamiConnector.class.getName());

    private final NamiServer server;
    private String username;
    private String password;

    private static final int INITIAL_LIMIT = 1000; // Maximale Anzahl der gefundenen Datensätze, wenn kein Limit vorgegeben wird.
    private static final int MAX_TAETIGKEITEN = 1000;

    private boolean isAuthenticated = false;

    public NamiConnector(NamiServer server) {
        this.server = server;
    }

    final CookieHandler cookieHandler = new CookieManager();

    private HttpClient getHttpClient() {
        return HttpClient
                .newBuilder()
                .cookieHandler(cookieHandler)
                .build();
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0)
                builder.append("&");
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    public void login(String username, String password) throws IOException, NamiLoginException, InterruptedException {
        if (isAuthenticated)
            return;
        this.username = username;
        this.password = password;
        Map<Object, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        data.put("redirectTo", "app.jsp");
        data.put("Login", "API");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(NamiURIBuilder.getLoginURIBuilder(server).build())
                .setHeader("content-type", "application/x-www-form-urlencoded")
                .POST(ofFormData(data))
                .build();
        HttpResponse<String> response = execute(request);
        if (response.statusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            // need to follow one redirect
            String redirectUrl = response.headers().map().get("Location").get(0);
            if (redirectUrl != null) {
                request = HttpRequest.newBuilder().uri(URI.create(redirectUrl)).GET().build();
                response = execute(request);
                log.info("Got redirect to: " + redirectUrl);
                if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                    isAuthenticated = true;
                    log.info("Authenticated to NaMi-Server with API.");
                }
            }
        } else { //login failed
            NamiResponse<Object> namiResponse = JsonUtil.fromJson(response.body(), new TypeToken<NamiResponse<Object>>(){}.getType());
            throw new NamiLoginException(namiResponse.getMessage());
        }
    }

    private HttpResponse<String> execute(HttpRequest request) throws IOException, InterruptedException {
        log.fine("Sending request to NaMi-Server: " + request.uri());
        return getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void checkResponse(HttpResponse<String> response) throws NamiException {
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            String redirectTarget = response.headers().firstValue("Location").orElse(null);
            if (redirectTarget != null) {
                log.warning("Got redirect to: " + redirectTarget);
                String redirectQuery = redirectTarget.substring(redirectTarget.indexOf('?') + 1);
                if (redirectTarget.contains("error.jsp")) {
                    String msg = URLDecoder.decode(redirectQuery, StandardCharsets.UTF_8).split("=", 2)[1];
                    throw new NamiException(msg);
                }
            }
            throw new NamiException("Statuscode of response is not 200 OK.");
        }
        String contentType = response.headers().firstValue("content-type").orElse(null);
        if (contentType == null)
            throw new NamiException("Response has no Content-Type.");
        else
            if (!contentType.equals("application/json") && !contentType.contains("application/json" + ";"))
                throw new NamiException("Content-Type of response is " + contentType + "; expected application/json.");
    }

    public <T> T executeApiRequest(HttpRequest request, final Type type) throws IOException, NamiException, InterruptedException {
        log.info("HTTP Call: " + request.uri().toString());
        if (!isAuthenticated)
            throw new NamiException("Did not login before API Request.");
        HttpResponse<String> response = execute(request);
        checkResponse(response);
        return JsonUtil.fromJson(response.body(), type);
    }

    public UriBuilder getURIBuilder(String path) {
        return getURIBuilder(path, true);
    }

    public UriBuilder getURIBuilder(String path, boolean restUrl) {
        return new NamiURIBuilder(server, path, restUrl);
    }

    // TODO: Warum NamiResponse nötig
    // -> gebe stattdessen direkt die Collection zurück oder null, wenn kein
    // success
    public NamiResponse<Collection<NamiMitglied>> getSearchResult(NamiSearchedValues searchedValues, int limit, int page, int start) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_NAMI_SEARCH)
                .setParameter("limit", Integer.toString(limit))
                .setParameter("page", Integer.toString(page))
                .setParameter("start", Integer.toString(start))
                .setParameter("searchedValues", JsonUtil.toJson(searchedValues));
        return executeApiRequest(
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
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_NAMI_MITGLIEDER)
                .appendPath("0")
                .appendPath(Integer.toString(id));
        NamiResponse<NamiMitglied> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<NamiMitglied>>() {}.getType());
        return (resp.isSuccess() ? resp.getData() : null);
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(NamiMitglied namiMitglied) throws IOException, NamiException, InterruptedException {
        return getSchulungen(namiMitglied.getId());
    }

    public Map<NamiBaustein, NamiSchulung> getSchulungen(int mitgliedsID) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_SCHULUNGEN)
                .appendPath(Integer.toString(mitgliedsID))
                .appendPath("/flist");
        NamiResponse<Collection<NamiSchulung>> response = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiSchulung>>>() {}.getType());
        return response.getData().stream()
                .collect(Collectors.toMap(NamiSchulung::getBaustein, Function.identity()));
    }

    public List<NamiEnum> getTaetigkeiten() throws NamiException, IOException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_TAETIGKEITEN);
        NamiResponse<List<NamiEnum>> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<List<NamiEnum>>>() {}.getType());
        return resp.getData();
    }

    public List<NamiEnum> getUntergliederungen() throws NamiException, IOException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_UNTERGLIEDERUNGEN);
        NamiResponse<List<NamiEnum>> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<List<NamiEnum>>>() {}.getType());
        return resp.getData();
    }

    public Collection<NamiMitglied> getMitgliederFromGruppierung(int gruppierungsnummer) throws NamiException, IOException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_NAMI_MITGLIEDER);
        builder.appendPath(Integer.toString(gruppierungsnummer))
                .appendPath("flist")
                .setParameter("limit", "5000")
                .setParameter("page", "1")
                .setParameter("start", "0");
        NamiResponse<Collection<NamiMitglied>> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiMitglied>>>() {}.getType());
        if (resp.isSuccess())
            return resp.getData();
        else
            throw new NamiException("Could not get member list from Nami: " + resp.getMessage());
    }

    public Collection<NamiTaetigkeitAssignment> getTaetigkeiten(int id) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_NAMI_TAETIGKEIT)
                .appendPath(Integer.toString(id))
                .appendPath("flist")
                .setParameter("limit", Integer.toString(MAX_TAETIGKEITEN))
                .setParameter("page", Integer.toString(0))
                .setParameter("start", Integer.toString(0));
        NamiResponse<Collection<NamiTaetigkeitAssignment>> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiTaetigkeitAssignment>>>() {}.getType());
        if (resp.isSuccess())
            return resp.getData();
        else
            return null;
    }

    public Collection<NamiGruppierung> getChildGruppierungen(int rootGruppierung) throws IOException, NamiException, InterruptedException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_GRUPPIERUNGEN)
                .appendPath(Integer.toString(rootGruppierung))
                .setParameter("node", Integer.toString(rootGruppierung));
        NamiResponse<Collection<NamiGruppierung>> resp = executeApiRequest(
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
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_GRUPPIERUNGEN);
        if (id != -1)
            builder.appendPath(String.valueOf(id));
        NamiResponse<Collection<NamiGruppierung>> resp = executeApiRequest(
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
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_GRUPPIERUNGEN)
                .appendPath("root")
                .setParameter("node", "root");
        NamiResponse<Collection<NamiGruppierung>> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<Collection<NamiGruppierung>>>() {}.getType());
        if (!resp.isSuccess())
            throw new NamiException("Could not get root Gruppierung");
        NamiGruppierung rootGrp = resp.getData().iterator().next();
        rootGrp.setChildren(null);
        return rootGrp;
    }

    public NamiTaetigkeitAssignment getTaetigkeit(int personId, int taetigkeitId) throws IOException, InterruptedException, NamiException {
        UriBuilder builder = getURIBuilder(NamiURIBuilder.URL_NAMI_TAETIGKEIT)
                .appendPath(Integer.toString(personId))
                .appendPath(Integer.toString(taetigkeitId));
        NamiResponse<NamiTaetigkeitAssignment> resp = executeApiRequest(
                HttpRequest.newBuilder().uri(builder.build()).GET().build(),
                new TypeToken<NamiResponse<NamiTaetigkeitAssignment>>() {}.getType());
        if (resp.isSuccess()) {
            return resp.getData();
        } else {
            return null;
        }
    }
}
