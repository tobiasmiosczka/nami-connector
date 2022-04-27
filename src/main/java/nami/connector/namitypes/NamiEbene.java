package nami.connector.namitypes;

import java.util.Arrays;

public enum NamiEbene {

    BUND(0, "bund"),
    DIOEZESE(2, "dioezese"),
    BEZIRK(4, "bezirk"),
    STAMM(6, "stamm");

    private final int significantChars;
    private final String name;

    NamiEbene(int significantChars, String name) {
        this.significantChars = significantChars;
        this.name = name;
    }

    public int getSignificantChars() {
        return significantChars;
    }

    public String getName() {
        return name;
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
        if (gruppierungId.substring(2).equals("0000"))
            return DIOEZESE;
        if (gruppierungId.substring(4).equals("00"))
            return BEZIRK;
        return STAMM;
    }

    public static NamiEbene fromString(String str) {
        return Arrays.stream(NamiEbene.values())
                .filter(e -> e.getName().equals(str))
                .findFirst()
                .orElse(null);
    }
}
