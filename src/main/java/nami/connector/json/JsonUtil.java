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
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.ThreadLocal.withInitial;
import static java.time.format.DateTimeFormatter.ofPattern;

public class JsonUtil {

    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class.getName());
    private static final ThreadLocal<DateTimeFormatter> DATE_FORMATTER = withInitial(() -> ofPattern("dd.MM.yyyy"));
    private static final ThreadLocal<DateTimeFormatter> DATE_TIME_FORMATTER = withInitial(() -> ofPattern("yyyy-MM-dd HH:mm:ss"));

    private static final Gson GSON = new GsonBuilder()

            .registerTypeAdapter(LocalDateTime.class, deserializer(JsonUtil::toLocalDateTime))
            .registerTypeAdapter(LocalDate.class, deserializer(JsonUtil::toLocalDate))

            .registerTypeAdapter(NamiEbene.class, deserializer(NamiEbene::fromString))
            .registerTypeAdapter(NamiBeitragsart.class, deserializer(NamiBeitragsart::fromString))
            .registerTypeAdapter(NamiGeschlecht.class, deserializer(NamiGeschlecht::fromString))

            .registerTypeAdapter(NamiBaustein.class, deserializer(NamiBaustein.class))
            .registerTypeAdapter(NamiMitgliedStatus.class, deserializer(NamiMitgliedStatus.class))
            .registerTypeAdapter(NamiMitgliedstyp.class, deserializer(NamiMitgliedstyp.class))
            .registerTypeAdapter(NamiStufe.class, deserializer(NamiStufe.class))
            .create();

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    private static <E extends Enum<E>> JsonDeserializer<E> deserializer(Class<E> eEnum) {
        Map<String, E> map = Arrays.stream(eEnum.getEnumConstants())
                .collect(Collectors.toMap(Enum::toString, Function.identity()));
        return deserializer(map, eEnum);
    }

    private static <T> JsonDeserializer<T> deserializer(Map<String, T> map, Class<T> tClass) {
        return deserializer(s -> {
            T result = map.get(s);
            if (result == null) {
                throw new IllegalArgumentException("Unexpected String for " + tClass.getName() + ": " + s);
            }
            return result;
        });
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
