package nami.connector.namitypes;

import java.util.Arrays;

public enum NamiBeitragsart {

    VOLLER_BEITRAG("Voller Beitrag"),
    FAMILIEN_BEITRAG("Familienermäßigt"),
    SOZIALERMAESSIGUNG("Sozialermäßigt"),
    VOLLER_BEITRAG_STIFTUNGSEURO("Voller Beitrag - Stiftungseuro"),
    FAMILIEN_BEITRAG_STIFTUNGSEURO("Familienermäßigt - Stiftungseuro"),
    SOZIALERMAESSIGUNG_STIFTUNGSEURO("Sozialermäßigt - Stiftungseuro"),
    KEIN_BEITRAG("NICHT_MITGLIEDER");

    private final String tag;

    NamiBeitragsart(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static NamiBeitragsart fromString(String string) {
        if(string == null || string.isEmpty())
            return KEIN_BEITRAG;
        return Arrays.stream(NamiBeitragsart.values())
                .filter(e -> e.getTag().equals(string))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unexpected String for Beitragsart:" + string));
    }
}
