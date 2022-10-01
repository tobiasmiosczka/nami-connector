package nami.connector.httpclient;

import nami.connector.NamiServer;
import nami.connector.exception.NamiException;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;

public interface NamiHttpClient {

    void login(NamiServer server, String username, String password) throws NamiException;

    <T> T executeApiRequest(HttpRequest request, Type type) throws NamiException;
}
