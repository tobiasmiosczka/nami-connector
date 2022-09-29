package nami.connector.namitypes;

public enum NamiStufe {

    BIBER("Biber"),
    WOELFLING("Wölfling"),
    JUNGPFADFINDER("Jungpfadfinder"),
    PFADFINDER("Pfadfinder"),
    ROVER("Rover"),
    ANDERE("Andere");

    private final String name;

    NamiStufe(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
