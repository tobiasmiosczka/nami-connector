package nami.connector;

import nami.connector.exception.NamiException;

import java.lang.reflect.Type;
import java.net.URI;

public class NamiApiException extends NamiException {

    private final Type type;
    private final URI uri;
    private final String message;

    public NamiApiException(Type type, URI uri, String message) {
        super("Error loading " + type.getTypeName() + " from " + uri.toString() + ". Message: " + message);
        this.type = type;
        this.uri = uri;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
