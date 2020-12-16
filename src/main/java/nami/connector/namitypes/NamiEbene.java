package nami.connector.namitypes;

public enum NamiEbene {

    BUND(0),
    DIOEZESE(2),
    BEZIRK(4),
    STAMM(6);

    private final int significantChars;

    NamiEbene(int significantChars) {
        this.significantChars = significantChars;
    }

    public int getSignificantChars() {
        return significantChars;
    }

    public static NamiEbene getFromGruppierungId(int gruppierungId) {
        // Fülle die GruppierungsID links mit Nullen auf 6 Stellen auf
        StringBuilder gruppierungsString = new StringBuilder(Integer.toString(gruppierungId));
        while (gruppierungsString.length() < 6)
            gruppierungsString.insert(0, "0");
        return getFromGruppierungId(gruppierungsString.toString());
    }

    public static NamiEbene getFromGruppierungId(String gruppierungId) {
        if (gruppierungId.equals("000000"))
            return BUND;
        else if (gruppierungId.substring(2).equals("0000"))
            return DIOEZESE;
        else if (gruppierungId.substring(4).equals("00"))
            return BEZIRK;
        else
            return STAMM;
    }

    public static NamiEbene fromString(String str) {
        if (str == null || str.isEmpty())
            return null;
        switch (str) {
        case "stamm":
            return STAMM;
        case "bezirk":
            return BEZIRK;
        case "dioezese":
            return DIOEZESE;
        case "bund":
            return BUND;
        default:
            return null;
        }
    }
}
