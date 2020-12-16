package nami.connector.namitypes;

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

    public static NamiBeitragsart fromString(String str) {
        if(str == null || str.isEmpty())
            return KEIN_BEITRAG;
        for(NamiBeitragsart beitragsart : NamiBeitragsart.values())
            if (beitragsart.getTag().equals(str))
                return beitragsart;
        throw new IllegalArgumentException("Unexpected String for Beitragsart:" + str);
    }
}
