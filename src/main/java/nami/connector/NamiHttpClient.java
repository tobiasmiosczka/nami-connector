package nami.connector;

import com.google.gson.reflect.TypeToken;
import nami.connector.exception.NamiException;
import nami.connector.exception.NamiLoginException;
import nami.connector.json.JsonUtil;
import nami.connector.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

public class NamiHttpClient {

    private static final Logger LOGGER = Logger.getLogger(NamiConnector.class.getName());

    final CookieHandler cookieHandler = new CookieManager();

    public void login(NamiServer server, String username, String password) throws IOException, NamiLoginException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(NamiUriBuilder.getLoginURIBuilder(server).build())
                .setHeader("content-type", "application/x-www-form-urlencoded")
                .POST(HttpUtil.ofFormData(buildLoginRequestFormData(username, password)))
                .build();
        HttpResponse<String> response = execute(request);
        if (response.statusCode() != HttpURLConnection.HTTP_MOVED_TEMP) { //login failed
            NamiResponse<Object> namiResponse = JsonUtil.fromJson(response.body(), new TypeToken<NamiResponse<Object>>(){}.getType());
            throw new NamiLoginException(namiResponse.getMessage());
        }
        // need to follow one redirect
        String redirectUrl = response.headers().map().get("Location").get(0);
        if (redirectUrl == null)
            throw new NamiLoginException("No redirect location.");
        response = execute(HttpRequest.newBuilder().uri(URI.create(redirectUrl)).GET().build());
        LOGGER.info("Got redirect to: " + redirectUrl);
        if (response.statusCode() != HttpURLConnection.HTTP_OK)
            throw new NamiLoginException("Login failed.");
        LOGGER.info("Authenticated to NaMi-Server with API.");
    }

    private static Map<String, String> buildLoginRequestFormData(String username, String password) {
        return Map.of(
                "username", username,
                "password", password,
                "redirectTo", "app.jsp",
                "Login", "API");
    }

    public <T> T executeApiRequest(HttpRequest request, final Type type) throws IOException, NamiException, InterruptedException {
        LOGGER.info("HTTP Call: " + request.uri().toString());
        HttpResponse<String> response = execute(request);
        checkResponse(response);
        return JsonUtil.fromJson(response.body(), type);
    }

    private HttpResponse<String> execute(HttpRequest request) throws IOException, InterruptedException {
        LOGGER.fine("Sending request to NaMi-Server: " + request.uri());
        return getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpClient getHttpClient() {
        return HttpClient
                .newBuilder()
                .cookieHandler(cookieHandler)
                .build();
    }

    private void checkResponse(HttpResponse<String> response) throws NamiException {
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            String redirectTarget = response.headers().firstValue("Location")
                    .orElseThrow(() -> new NamiException("Statuscode of response is not 200 OK."));
            LOGGER.warning("Got redirect to: " + redirectTarget);
            String redirectQuery = redirectTarget.substring(redirectTarget.indexOf('?') + 1);
            if (redirectTarget.contains("error.jsp"))
                throw new NamiException(URLDecoder.decode(redirectQuery, StandardCharsets.UTF_8).split("=", 2)[1]);

        }
        String contentType = response.headers().firstValue("content-type")
                .orElseThrow(() -> new NamiException("Response has no Content-Type."));
        if (!contentType.equals("application/json") && !contentType.contains("application/json" + ";"))
            throw new NamiException("Content-Type of response is " + contentType + "; expected application/json.");
    }

}
