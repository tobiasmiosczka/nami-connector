package nami.connector.namitypes;

public enum NamiMitgliedstyp {
    MITGLIED("Mitglied"),
    NICHT_MITGLIED("Nicht-Mitglied"),
    SCHNUPPER_MITGLIED("Schnuppermitglied");

    private final String tag;

    NamiMitgliedstyp(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String toString() {
        return this.name();
    }

    public static NamiMitgliedstyp fromString(String str) {
        if (str == null)
            throw new IllegalArgumentException("Unexpected String for Mitgliedstyp:" + str);
        for (NamiMitgliedstyp mitgliedstyp : NamiMitgliedstyp.values())
            if (mitgliedstyp.getTag().equals(str))
                return mitgliedstyp;
        throw new IllegalArgumentException("Unexpected String for Mitgliedstyp: " + str);
    }
}
