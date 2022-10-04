package nami.connector.httpclient;

import nami.connector.NamiServer;
import nami.connector.exception.NamiException;

import java.net.URI;
import java.util.List;

public interface NamiHttpClient {

    void login(NamiServer server, String username, String password) throws NamiException;

    <T> T getSingle(final URI uri, final Class<T> tClass) throws NamiException;
    <T> List<T> getList(final URI uri, final Class<T> tClass) throws NamiException;
}
