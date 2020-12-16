open module nami.connector {
    requires transitive com.google.gson;
    requires transitive java.net.http;

    exports nami.connector;
    exports nami.connector.exception;
    exports nami.connector.namitypes;
}