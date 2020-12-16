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
        switch (str) {
            case "männlich":
            case "MAENNLICH":
                return MAENNLICH;
            case "weiblich":
            case "WEIBLICH":
                return WEIBLICH;
            case "keine Angabe":
            case "Keine Angabe":
            case "KEINE_ANGABE":
                return KEINE_ANGABE;
            case "":
                return null;
            default:
                throw new IllegalArgumentException("Unexpected String for Geschlecht: " + str);
        }
    }
}