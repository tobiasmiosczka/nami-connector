package nami.connector.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import nami.connector.namitypes.NamiBaustein;
import nami.connector.namitypes.NamiBeitragsart;
import nami.connector.namitypes.NamiEbene;
import nami.connector.namitypes.NamiGeschlecht;
import nami.connector.namitypes.NamiMitgliedStatus;
import nami.connector.namitypes.NamiMitgliedstyp;
import nami.connector.namitypes.NamiStufe;

import java.io.Reader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class JsonUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (j, t, c) -> {
                        String string = j.getAsString();
                        if(string == null || string.equals("")) {
                            return null;
                        }
                        try {
                            return LocalDateTime.from(DATE_TIME_FORMATTER.parse(string));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
            .registerTypeAdapter(
                    LocalDate.class,
                    (JsonDeserializer<LocalDate>) (j, t, c) -> {
                        String string = j.getAsString();
                        if(string == null || string.equals(""))
                            return null;
                        try {
                            return LocalDate.from(DATE_FORMATTER.parse(string));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
            .registerTypeAdapter(
                    NamiEbene.class,
                    (JsonDeserializer<NamiEbene>) (j, t, c) -> NamiEbene.fromString(j.getAsString()))
            .registerTypeAdapter(
                    NamiBeitragsart.class,
                    (JsonDeserializer<NamiBeitragsart>) (j, t, c) -> NamiBeitragsart.fromString(j.getAsString()))
            .registerTypeAdapter(
                    NamiGeschlecht.class,
                    (JsonDeserializer<NamiGeschlecht>) (j, t, c) -> NamiGeschlecht.fromString(j.getAsString()))
            .registerTypeAdapter(
                    NamiMitgliedStatus.class,
                    (JsonDeserializer<NamiMitgliedStatus>) (j, t, c) -> NamiMitgliedStatus.fromString(j.getAsString()))
            .registerTypeAdapter(
                    NamiMitgliedstyp.class,
                    (JsonDeserializer<NamiMitgliedstyp>) (j, t, c) -> NamiMitgliedstyp.fromString(j.getAsString()))
            .registerTypeAdapter(
                    NamiStufe.class,
                    (JsonDeserializer<NamiStufe>) (j, t, c) -> NamiStufe.fromString(j.getAsString()))
            .registerTypeAdapter(
                    NamiBaustein.class,
                    (JsonDeserializer<NamiBaustein>) (j, t, c) -> NamiBaustein.fromString(j.getAsString()))
            .create();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <T> T fromJson(Reader json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}
