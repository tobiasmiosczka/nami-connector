package nami.connector.uri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nami.connector.namitypes.NamiSearchedValues;

import java.net.URI;

public class NamiUriFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

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
    public static final String PARAMETER_LIMIT = "limit";
    public static final String PARAMETER_PAGE = "page";
    public static final String PARAMETER_START = "start";
    public static final String PATH_FLIST = "flist";
    public static final String PARAMETER_NODE = "node";
    public static final String PARAMETER_SEARCHED_VALUES = "searchedValues";
    public static final String PATH_ROOT = "root";

    private final UriBuilder template;

    public NamiUriFactory(URI baseUri) {
        this.template = UriBuilder.fromUri(baseUri);
    }

    public URI namiSearch(int limit, int page, int start, NamiSearchedValues searchedValues) {
        try {
            return rest()
                    .appendPath(URL_NAMI_SEARCH)
                    .withParameter(PARAMETER_LIMIT, limit)
                    .withParameter(PARAMETER_PAGE, page)
                    .withParameter(PARAMETER_START, start)
                    .withParameter(PARAMETER_SEARCHED_VALUES, OBJECT_MAPPER.writeValueAsString(searchedValues))
                    .build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null; //TODO: exception Handling
        }
    }

    public URI namiMitglieder(int id) {
        return rest()
                .appendPath(URL_NAMI_MITGLIEDER)
                .appendPath(0)
                .appendPath(id)
                .build();
    }

    public URI namiSchulungen(int userId) {
        return rest()
                .appendPath(URL_SCHULUNGEN)
                .appendPath(userId)
                .appendPath(PATH_FLIST)
                .build();
    }

    public URI namiTaetigkeiten() {
        return rest()
                .appendPath(URL_TAETIGKEITEN)
                .build();
    }

    public URI namiUntergliederungen() {
        return rest()
                .appendPath(URL_UNTERGLIEDERUNGEN)
                .build();
    }

    public URI memberFromGroup(int gruppierungsnummer) {
        return rest()
                .appendPath(URL_NAMI_MITGLIEDER)
                .appendPath(gruppierungsnummer)
                .appendPath(PATH_FLIST)
                .withParameter(PARAMETER_LIMIT, 5000)
                .withParameter(PARAMETER_PAGE, 1)
                .withParameter(PARAMETER_START, 0)
                .build();
    }

    public URI taetigkeitByPersonIdAndTeatigkeitId(int personId, int taetigkeitId) {
        return rest()
                .appendPath(URL_NAMI_TAETIGKEIT)
                .appendPath(personId)
                .appendPath(taetigkeitId)
                .build();
    }

    public URI rootGroupWithoutChildren() {
        return rest()
                .appendPath(URL_GRUPPIERUNGEN)
                .appendPath(PATH_ROOT)
                .withParameter(PARAMETER_NODE, PATH_ROOT)
                .build();
    }

    public URI groupsByUser(int id) {
        UriBuilder builder = rest()
                .appendPath(URL_GRUPPIERUNGEN);
        if (id != -1)
            builder.appendPath(id);
        return builder.build();
    }

    public URI childGroups(int rootGruppierung) {
        return rest()
                .appendPath(URL_GRUPPIERUNGEN)
                .appendPath(rootGruppierung)
                .withParameter(PARAMETER_NODE, rootGruppierung)
                .build();
    }

    public URI namiTaetigkeiten(int id) {
        return rest()
                .appendPath(URL_NAMI_TAETIGKEIT)
                .appendPath(id)
                .appendPath(PATH_FLIST)
                .withParameter(PARAMETER_LIMIT, MAX_TAETIGKEITEN)
                .withParameter(PARAMETER_PAGE, 0)
                .withParameter(PARAMETER_START, 0)
                .build();
    }

    public URI login() {
        return template.copy()
                .appendPath(URL_NAMI_STARTUP)
                .build();
    }

    private UriBuilder rest() {
        return template.copy()
                .appendPath("rest/api/2/2/service");
    }

}
