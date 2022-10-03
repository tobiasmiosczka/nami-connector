package nami.connector.uri;

import nami.connector.NamiServer;
import nami.connector.httpclient.impl.JsonUtil;
import nami.connector.namitypes.NamiSearchedValues;

import java.net.URI;

public class NamiUriFactory {

    private static final JsonUtil jsonUtil = new JsonUtil();

    // URL, die zum Login in NaMi verwendet wird.
    static final String URL_NAMI_STARTUP = "/rest/nami/auth/manual/sessionStartup";

    // URL, mit der die Root-Gruppierung und die Kinder für jede Gruppierung abgefragt werden.
    private static final String URL_GRUPPIERUNGEN = "/nami/gruppierungen/filtered-for-navigation/gruppierung/node";

    // URL, mit der der Datensatz eines Mitglieds (identifiziert durch seine ID) abgefragt wird.
    // Am Ende der URL müsste eigentlich die GruppierungsID angegeben sein.
    // Scheinbar kann man aber auch immer "0" angeben und bekommt
    // trotzdem jedes Mitglied geliefert
    private static final String URL_NAMI_MITGLIEDER = "/nami/mitglied/filtered-for-navigation/gruppierung/gruppierung/";

    // URL, mit der eine Tätigkeitszuordnung eines Mitglieds abgefragt wird.
    private static final String URL_NAMI_TAETIGKEIT = "/nami/zugeordnete-taetigkeiten/filtered-for-navigation/gruppierung-mitglied/mitglied";

    // URL, mit der die Beitragszahlungen eines Mitglieds abgefragt werden können.
    private static final String URL_BEITRAGSZAHLUNGEN = "/mgl-verwaltungS/beitrKonto-anzeigen";

    // URL, um eine Suchanfrage an NaMi zu senden.
    private static final String URL_NAMI_SEARCH = "/nami/search/result-list";

    // URL, mit der alle verfügbaren Tätigkeiten abgefragt werden können.
    private static final String URL_TAETIGKEITEN = "/system/taetigkeit";

    // URL, mit der alle verfügbaren Untergliederungen abgefragt werden können.
    private static final String URL_UNTERGLIEDERUNGEN = "/orgadmin/untergliederung";

    private static final String URL_SCHULUNGEN = "/nami/mitglied-ausbildung/filtered-for-navigation/mitglied/mitglied";

    private static final int MAX_TAETIGKEITEN = 1000;

    private final NamiServer namiServer;

    public NamiUriFactory(NamiServer namiServer) {
        this.namiServer = namiServer;
    }

    public URI namiSearch(int limit, int page, int start, NamiSearchedValues searchedValues) {
        return getURIBuilder(URL_NAMI_SEARCH)
                .setParameter("limit", limit)
                .setParameter("page", page)
                .setParameter("start", start)
                .setParameter("searchedValues", jsonUtil.toJson(searchedValues))
                .build();
    }

    public URI namiMitglieder(int id) {
        return getURIBuilder(URL_NAMI_MITGLIEDER)
                .appendPath(0)
                .appendPath(id)
                .build();
    }

    public URI namiSchulungen(int userId) {
        return getURIBuilder(URL_SCHULUNGEN)
                .appendPath(userId)
                .appendPath("flist")
                .build();
    }

    public URI namiTaetigkeiten() {
        return getURIBuilder(URL_TAETIGKEITEN)
                .build();
    }

    public URI namiUntergliederungen() {
        return getURIBuilder(URL_UNTERGLIEDERUNGEN)
                .build();
    }

    public URI memberFromGroup(int gruppierungsnummer) {
        return getURIBuilder(URL_NAMI_MITGLIEDER)
                .appendPath(gruppierungsnummer)
                .appendPath("flist")
                .setParameter("limit", 5000)
                .setParameter("page", 1)
                .setParameter("start", 0)
                .build();
    }

    public URI taetigkeitByPersonIdAndTeatigkeitId(int personId, int taetigkeitId) {
        return getURIBuilder(URL_NAMI_TAETIGKEIT)
                .appendPath(personId)
                .appendPath(taetigkeitId)
                .build();
    }

    public URI rootGroupWithoutChildren() {
        return getURIBuilder(URL_GRUPPIERUNGEN)
                .appendPath("root")
                .setParameter("node", "root")
                .build();
    }

    public URI groupsByUser(int id) {
        UriBuilder builder = getURIBuilder(URL_GRUPPIERUNGEN);
        if (id != -1)
            builder.appendPath(id);
        return builder.build();
    }

    public URI childGroups(int rootGruppierung) {
        return getURIBuilder(URL_GRUPPIERUNGEN)
                .appendPath(rootGruppierung)
                .setParameter("node", rootGruppierung)
                .build();
    }

    public URI namiTaetigkeiten(int id) {
        return getURIBuilder(URL_NAMI_TAETIGKEIT)
                .appendPath(id)
                .appendPath("flist")
                .setParameter("limit", MAX_TAETIGKEITEN)
                .setParameter("page", 0)
                .setParameter("start", 0)
                .build();
    }

    private UriBuilder getURIBuilder(String path) {
        return new NamiUriBuilder(namiServer, path, true);
    }
}
