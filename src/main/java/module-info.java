open module nami.connector {
    requires transitive com.google.gson;
    requires transitive java.net.http;
    requires java.logging;

    exports nami.connector;
    exports nami.connector.exception;
    exports nami.connector.namitypes;
    exports nami.connector.httpclient;
    exports nami.connector.httpclient.impl;
    exports nami.connector.uri;
}