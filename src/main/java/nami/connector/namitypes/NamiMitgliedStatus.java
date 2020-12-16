package nami.connector.namitypes;

public enum NamiMitgliedStatus {

    AKTIV("Aktiv"),
    INAKTIV("Inaktiv");

    private final String tag;

    NamiMitgliedStatus(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static NamiMitgliedStatus fromString(String str) {
        if (str == null ||str.isEmpty())
            throw new IllegalArgumentException("Unexpected String for MitgliedStatus: " + str);
        for (NamiMitgliedStatus mitgliedStatus : NamiMitgliedStatus.values())
            if (mitgliedStatus.getTag().equals(str))
                return mitgliedStatus;
        throw new IllegalArgumentException("Unexpected String for MitgliedStatus: " + str);
    }
}
