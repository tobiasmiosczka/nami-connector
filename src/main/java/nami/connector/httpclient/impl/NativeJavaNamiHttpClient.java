package nami.connector.httpclient.impl;

import nami.connector.*;
import nami.connector.exception.NamiApiException;
import nami.connector.exception.NamiException;
import nami.connector.exception.NamiLoginException;
import nami.connector.httpclient.NamiHttpClient;
import nami.connector.namitypes.NamiLoginResponse;
import nami.connector.namitypes.NamiResponse;
import nami.connector.uri.NamiUriBuilder;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class NativeJavaNamiHttpClient implements NamiHttpClient {

    private static final Logger LOGGER = Logger.getLogger(NamiConnector.class.getName());

    private final CookieHandler cookieHandler = new CookieManager();

    private static HttpRequest buildLoginRequest(final NamiServer server, final String username, final String password) {
        return new FormDataHttpRequestBuilder()
                .uri(NamiUriBuilder.getLoginURIBuilder(server).build())
                .withValue("username", username)
                .withValue("password", password)
                .withValue("redirectTo", "app.jsp")
                .withValue("Login", "API")
                .build();
    }

    private HttpClient getHttpClient() {
        return HttpClient
                .newBuilder()
                .cookieHandler(cookieHandler)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public void login(final NamiServer server, final String username, final String password) throws NamiException {
        HttpResponse<NamiLoginResponse> response = loginRequest(buildLoginRequest(server, username, password));
        if (response.statusCode() != HttpURLConnection.HTTP_OK)
            throw new NamiLoginException("Status code is " + response.statusCode() + ".");
        LOGGER.info("Authenticated to NaMi-Server with API.");
    }

    @Override
    public <T> T getSingle(final URI uri, final Class<T> tClass) throws NamiException {
        return sendNamiApiRequest(uri, NamiResponseBodyHandler.singleHandler(tClass));
    }

    @Override
    public <T> List<T> getList(final URI uri, final Class<T> tClass) throws NamiException {
        return sendNamiApiRequest(uri, NamiResponseBodyHandler.listHandler(tClass));
    }

    private <T> T sendNamiApiRequest(URI uri, JacksonBodyHandler<NamiResponse<T>> responseBodyHandler) throws NamiException {
        try {
            HttpResponse<NamiResponse<T>> response = getHttpClient().send(buildGetRequest(uri), responseBodyHandler);
            validateResponse(response);
            return response.body().getData();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new NamiApiException(responseBodyHandler.getType(), uri, e.getMessage());
        }
    }

    private HttpResponse<NamiLoginResponse> loginRequest(final HttpRequest request) throws NamiLoginException {
        LOGGER.fine("Sending request to NaMi-Server: " + request.uri());
        try {
            return getHttpClient().send(request, NamiResponseBodyHandler.loginHandler());
        } catch (Exception e) {
            e.printStackTrace();
            throw new NamiLoginException(e);
        }
    }

    private static <T> void validateResponse(final HttpResponse<NamiResponse<T>> response) throws NamiException {
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            String redirectTarget = response.headers().firstValue("Location")
                    .orElseThrow(() -> new NamiException("Status code of response is not 200 OK."));
            LOGGER.warning("Got redirect to: " + redirectTarget);
            String redirectQuery = redirectTarget.substring(redirectTarget.indexOf('?') + 1);
            if (redirectTarget.contains("error.jsp")) {
                throw new NamiException(URLDecoder.decode(redirectQuery, StandardCharsets.UTF_8).split("=", 2)[1]);
            }
        }
        String contentType = response.headers().firstValue("content-type")
                .orElseThrow(() -> new NamiException("Response has no Content-Type."));
        if (!"application/json".equals(contentType) && !contentType.contains("application/json;")) {
            throw new NamiException("Content-Type of response is " + contentType + "; expected application/json.");
        }
        if (!response.body().isSuccess()) {
            throw new NamiException(response.body().getMessage());
        }
    }

    private static HttpRequest buildGetRequest(final URI uri) {
        return HttpRequest.newBuilder().uri(uri).GET().build();
    }
}
