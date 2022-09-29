package nami.connector.namitypes;

public enum NamiMitgliedstyp {
    MITGLIED("Mitglied"),
    NICHT_MITGLIED("Nicht-Mitglied"),
    SCHNUPPER_MITGLIED("Schnuppermitglied");

    private final String name;

    NamiMitgliedstyp(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
