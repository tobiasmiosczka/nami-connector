package nami.connector.namitypes;

public class NamiResponse<T> {

    private boolean success;
    private T data;
    private int totalEntries;
    private String message;
    // private String responseType;

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public String getMessage() {
        return message;
    }
}
