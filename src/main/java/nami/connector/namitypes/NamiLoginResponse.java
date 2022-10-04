package nami.connector.namitypes;

public class NamiLoginResponse {

    private int statusCode;
    private String statusMessage;
    private String apiSessionName;
    private String apiSessionToken;
    private int minorNumber;
    private int majorNumber;

    public NamiLoginResponse() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getApiSessionName() {
        return apiSessionName;
    }

    public String getApiSessionToken() {
        return apiSessionToken;
    }

    public int getMinorNumber() {
        return minorNumber;
    }

    public int getMajorNumber() {
        return majorNumber;
    }
}
