package nami.connector.uri;

import nami.connector.NamiServer;

import static nami.connector.uri.NamiUriFactory.URL_NAMI_STARTUP;

public class NamiUriBuilder extends UriBuilder {

    public NamiUriBuilder(NamiServer server, String path, boolean restUrl) {
        super();
        if (server.getUseSsl())
            setScheme("https");
        else
            setScheme("http");
        setHost(server.getNamiServer());
        setPath("/" + server.getNamiDeploy());
        if (restUrl)
            appendPath("rest/api/2/2/service");
        appendPath(path);
    }

    public static NamiUriBuilder getLoginURIBuilder(NamiServer server) {
        return new NamiUriBuilder(server, URL_NAMI_STARTUP, false);
    }

}
