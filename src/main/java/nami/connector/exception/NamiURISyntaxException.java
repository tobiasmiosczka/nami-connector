package nami.connector.exception;

import java.net.URISyntaxException;

public class NamiURISyntaxException extends RuntimeException {

    private static final long serialVersionUID = -4918799375724605164L;

    public NamiURISyntaxException(URISyntaxException cause) {
        super(cause);
    }

}
