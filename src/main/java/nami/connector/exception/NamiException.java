package nami.connector.exception;

public class NamiException extends Exception {
    private static final long serialVersionUID = -4261770474727109255L;

    public NamiException() {
        super();
    }

    public NamiException(String str) {
        super(str);
    }

    public NamiException(Throwable cause) {
        super(cause);
    }

}
