package nami.connector.httpclient.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import nami.connector.namitypes.NamiBaustein;
import nami.connector.namitypes.NamiBeitragsart;
import nami.connector.namitypes.NamiEbene;
import nami.connector.namitypes.NamiGeschlecht;
import nami.connector.namitypes.NamiMitgliedStatus;
import nami.connector.namitypes.NamiMitgliedstyp;
import nami.connector.namitypes.NamiStufe;

import java.io.IOException;
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

    private static final Module NAMI_MODULE = new SimpleModule()
            .addDeserializer(LocalDateTime.class, deserializer(JsonUtil::toLocalDateTime, LocalDateTime.class))
            .addDeserializer(LocalDate.class, deserializer(JsonUtil::toLocalDate, LocalDate.class))
            .addDeserializer(NamiEbene.class, deserializer(NamiEbene::fromString, NamiEbene.class))
            .addDeserializer(NamiBeitragsart.class, deserializer(NamiBeitragsart::fromString, NamiBeitragsart.class))
            .addDeserializer(NamiGeschlecht.class, deserializer(NamiGeschlecht::fromString, NamiGeschlecht.class))
            .addDeserializer(NamiBaustein.class, deserializer(NamiBaustein.class))
            .addDeserializer(NamiMitgliedStatus.class, deserializer(NamiMitgliedStatus.class))
            .addDeserializer(NamiMitgliedstyp.class, deserializer(NamiMitgliedstyp.class))
            .addDeserializer(NamiStufe.class, deserializer(NamiStufe.class));

    private final ObjectMapper objectMapper = prepareObjectMapper();

    public String toJson(Object o) {
        try {
            return this.objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T fromJson(String json, Type type) {
        try {
            return objectMapper.readValue(json, TypeFactory.defaultInstance().constructType(type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
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
        }, tClass);
    }

    private static <T> JsonDeserializer<T> deserializer(Function<String, T> function, Class<T> tClass) {
        return new StdDeserializer<>(tClass) {
            @Override
            public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return function.apply(p.getValueAsString());
            }
        };
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

    public static ObjectMapper prepareObjectMapper() {
        return new ObjectMapper()
                .registerModule(NAMI_MODULE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
