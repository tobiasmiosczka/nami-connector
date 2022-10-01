package nami.connector.namitypes;

public enum NamiMitgliedStatus {

    AKTIV("Aktiv"),
    INAKTIV("Inaktiv");

    private final String name;

    NamiMitgliedStatus(String name) {
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
