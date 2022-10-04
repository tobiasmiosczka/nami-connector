package nami.connector.httpclient.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import nami.connector.namitypes.NamiLoginResponse;
import nami.connector.namitypes.NamiResponse;

import java.util.ArrayList;
import java.util.List;

public class NamiResponseBodyHandler<T> extends JacksonBodyHandler<T> {

    private static final TypeFactory FACTORY = TypeFactory.defaultInstance();

    private NamiResponseBodyHandler(JavaType type) {
        super(type, JsonUtil.prepareObjectMapper());
    }

    private static <T> JavaType buildNamiResponseType(Class<T> tClass) {
        return FACTORY.constructParametricType(NamiResponse.class, FACTORY.constructType(tClass));
    }

    private static <T> JavaType buildNamiResponseListType(Class<T> tClass) {
        return FACTORY.constructParametricType(NamiResponse.class, FACTORY.constructCollectionType(ArrayList.class, FACTORY.constructType(tClass)));
    }

    public static <T> JacksonBodyHandler<NamiResponse<T>> singleHandler(Class<T> tClass) {
        JavaType type = buildNamiResponseType(tClass);
        return new NamiResponseBodyHandler<>(type);
    }

    public static <T> JacksonBodyHandler<NamiResponse<List<T>>> listHandler(Class<T> tClass) {
        JavaType type = buildNamiResponseListType(tClass);
        return new NamiResponseBodyHandler<>(type);
    }

    public static JacksonBodyHandler<NamiLoginResponse> loginHandler() {
        JavaType type = FACTORY.constructType(NamiLoginResponse.class);
        return new NamiResponseBodyHandler<>(type);
    }
}
