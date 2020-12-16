package nami.connector.exception;

public class NamiLoginException extends NamiException {
    private static final long serialVersionUID = -5171203317792006455L;

    public NamiLoginException(String message) {
        super(message);
    }

    public NamiLoginException(Throwable cause) {
        super(cause);
    }

}
