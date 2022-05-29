package nami.connector.httpclient;

import nami.connector.NamiServer;
import nami.connector.exception.NamiException;
import nami.connector.exception.NamiLoginException;
import nami.connector.namitypes.NamiResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;

public interface NamiHttpClient {

    void login(NamiServer server, String username, String password) throws IOException, NamiLoginException, InterruptedException;

    <T> NamiResponse<T> executeApiRequest(HttpRequest request, Type type) throws IOException, NamiException, InterruptedException;
}
