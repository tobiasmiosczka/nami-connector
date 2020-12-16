package nami.connector;

public class NamiResponse<DataT> {
    // Die folgenden Variablen stammen aus NaMi. Keinesfalls umbenennen.
    private boolean success;
    private DataT data;
    private int totalEntries;
    private String message;
    // private String responseType;

    public boolean isSuccess() {
        return success;
    }

    public DataT getData() {
        return data;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public String getMessage() {
        return message;
    }
}
