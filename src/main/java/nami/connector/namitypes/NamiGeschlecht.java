package nami.connector.namitypes;

public enum NamiGeschlecht {
    MAENNLICH("männlich"),
    WEIBLICH("weiblich"),
    KEINE_ANGABE("");

    private final String tag;

    NamiGeschlecht(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static NamiGeschlecht fromString(String str) {
        return switch (str) {
            case "männlich", "MAENNLICH" -> MAENNLICH;
            case "weiblich", "WEIBLICH" -> WEIBLICH;
            case "keine Angabe", "Keine Angabe", "KEINE_ANGABE" -> KEINE_ANGABE;
            case "" -> null;
            default -> throw new IllegalArgumentException("Unexpected String for Geschlecht: " + str);
        };
    }
}