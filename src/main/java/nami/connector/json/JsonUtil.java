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

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.ThreadLocal.withInitial;
import static java.time.format.DateTimeFormatter.ofPattern;

public class JsonUtil {

    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class.getName());
    private static final ThreadLocal<DateTimeFormatter> DATE_FORMATTER = withInitial(() -> ofPattern("dd.MM.yyyy"));
    private static final ThreadLocal<DateTimeFormatter> DATE_TIME_FORMATTER = withInitial(() -> ofPattern("yyyy-MM-dd HH:mm:ss"));

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, deserializer(JsonUtil::toLocalDateTime))
            .registerTypeAdapter(LocalDate.class, deserializer(JsonUtil::toLocalDate))
            .registerTypeAdapter(NamiEbene.class, deserializer(NamiEbene::fromString))
            .registerTypeAdapter(NamiBeitragsart.class, deserializer(NamiBeitragsart::fromString))
            .registerTypeAdapter(NamiGeschlecht.class, deserializer(NamiGeschlecht::fromString))
            .registerTypeAdapter(NamiMitgliedStatus.class, deserializer(NamiMitgliedStatus::fromString))
            .registerTypeAdapter(NamiMitgliedstyp.class, deserializer(NamiMitgliedstyp::fromString))
            .registerTypeAdapter(NamiStufe.class, deserializer(NamiStufe::fromString))
            .registerTypeAdapter(NamiBaustein.class, deserializer(NamiBaustein::fromString))
            .create();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    private static <T> JsonDeserializer<T> deserializer(Function<String, T> function) {
        return (j, t, c) -> function.apply(j.getAsString());
    }

    private static LocalDateTime toLocalDateTime(String s) {
        if(s == null || s.equals(""))
            return null;
        return tryGetOrNull(() -> LocalDateTime.from(DATE_TIME_FORMATTER.get().parse(s)));
    }

    private static LocalDate toLocalDate(String s) {
        if(s == null || s.equals(""))
            return null;
        return tryGetOrNull(() -> LocalDate.from(DATE_FORMATTER.get().parse(s)));
    }

    private static <T> T tryGetOrNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not deserialize. ", e);
            return null;
        }
    }
}
