package nami.connector;

public class NamiURIBuilder extends UriBuilder {
    // URL, die zum Login in NaMi verwendet wird.
    private static final String URL_NAMI_STARTUP = "/rest/nami/auth/manual/sessionStartup";

    // URL, mit der die Root-Gruppierung und die Kinder für jede Gruppierung abgefragt werden.
    public static final String URL_GRUPPIERUNGEN = "/nami/gruppierungen/filtered-for-navigation/gruppierung/node";

    // URL, mit der der Datensatz eines Mitglieds (identifiziert durch seine ID) abgefragt wird.
    // Am Ende der URL müsste eigentlich die GruppierungsID angegeben sein.
    // Scheinbar kann man aber auch immer "0" angeben und bekommt
    // trotzdem jedes Mitglied geliefert
    public static final String URL_NAMI_MITGLIEDER = "/nami/mitglied/filtered-for-navigation/gruppierung/gruppierung/";

    // URL, mit der eine Tätigkeitszuordnung eines Mitglieds abgefragt wird.
    public static final String URL_NAMI_TAETIGKEIT = "/nami/zugeordnete-taetigkeiten/filtered-for-navigation/gruppierung-mitglied/mitglied";

    // URL, mit der die Beitragszahlungen eines Mitglieds abgefragt werden können.
    public static final String URL_BEITRAGSZAHLUNGEN = "/mgl-verwaltungS/beitrKonto-anzeigen";

    // URL, um eine Suchanfrage an NaMi zu senden.
    public static final String URL_NAMI_SEARCH = "/nami/search/result-list";

    // URL, mit der alle verfügbaren Tätigkeiten abgefragt werden können.
    public static final String URL_TAETIGKEITEN = "/system/taetigkeit";

    // URL, mit der alle verfügbaren Untergliederungen abgefragt werden können.
    public static final String URL_UNTERGLIEDERUNGEN = "/orgadmin/untergliederung";

    public static final String URL_SCHULUNGEN = "/nami/mitglied-ausbildung/filtered-for-navigation/mitglied/mitglied";


    public NamiURIBuilder(NamiServer server, String path, boolean restUrl) {
        super();
        if (server.getUseSsl())
            setScheme("https");
        else
            setScheme("http");
        setHost(server.getNamiServer());
        setPath("/" + server.getNamiDeploy());
        if (restUrl)
            appendPath("rest/api/2/2/service");
        appendPath(path);
    }

    public static NamiURIBuilder getLoginURIBuilder(NamiServer server) {
        return new NamiURIBuilder(server, URL_NAMI_STARTUP, false);
    }

}
