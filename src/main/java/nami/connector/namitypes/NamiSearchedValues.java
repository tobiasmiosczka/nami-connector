package nami.connector.namitypes;

public class NamiSearchedValues {
    private String vorname = "";
    private String nachname = "";
    private String alterVon = "";
    private String alterBis = "";
    private String mglWohnort = "";
    private String mitgliedsNummber = ""; // Rechtschreibfehler in NaMi
    private String mglStatusId = null;
    private String mglTypeId = null;
    private Integer tagId = null;
    private Integer bausteinIncludedId = null;
    private boolean zeitschriftenversand = false;

    private Integer untergliederungId = null;
    private Integer taetigkeitId = null;
    private boolean mitAllenTaetigkeiten = false;
    private boolean withEndedTaetigkeiten = false;
    private Integer ebeneId = null;
    private String grpNummer = "";
    private String grpName = "";
    private Integer gruppierungDioezeseId = null;
    private Integer gruppierungBezirkId = null;
    private Integer gruppierungStammId = null;
    private boolean inGrp = false;
    private boolean unterhalbGrp = false;

    private String id = "";
    private String searchName = "";

    public NamiSearchedValues setMitgliedsnummer(String mitgliedsnummer) {
        this.mitgliedsNummber = mitgliedsnummer;
        return this;
    }

    public NamiSearchedValues setUntergliederungId(Integer untergliederungId) {
        this.untergliederungId = untergliederungId;
        return this;
    }

    public NamiSearchedValues setTaetigkeitId(Integer taetigkeitId) {
        this.taetigkeitId = taetigkeitId;
        return this;
    }

    public NamiSearchedValues setNachname(String nachname) {
        this.nachname = nachname;
        return this;
    }

    public NamiSearchedValues setVorname(String vorname) {
        this.vorname = vorname;
        return this;
    }

    public NamiSearchedValues setMitgliedStatus(NamiMitgliedStatus status) {
        this.mglStatusId = status.toString();
        return this;
    }

    public NamiSearchedValues setMitgliedstyp(NamiMitgliedstyp mgltype) {
        this.mglTypeId = mgltype.toString();
        return this;
    }

    public NamiSearchedValues setGruppierungsnummer(String gruppierungsnummer) {
        this.grpNummer = gruppierungsnummer;
        return this;
    }

    public NamiSearchedValues setMitAllenTaetigkeiten(boolean mitAllenTaetigkeiten) {
        this.mitAllenTaetigkeiten = mitAllenTaetigkeiten;
        return this;
    }
}
