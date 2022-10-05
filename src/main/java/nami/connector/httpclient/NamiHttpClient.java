package nami.connector.httpclient;

import nami.connector.NamiServer;
import nami.connector.exception.NamiException;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NamiHttpClient {

    void login(NamiServer server, String username, String password) throws NamiException;

    <T> CompletableFuture<T> getSingle(final URI uri, final Class<T> tClass);
    <T> CompletableFuture<List<T>> getList(final URI uri, final Class<T> tClass);
}
