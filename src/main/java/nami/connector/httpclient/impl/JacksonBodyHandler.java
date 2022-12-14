package nami.connector.httpclient.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class JacksonBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private final JavaType typeReference;
    private final ObjectMapper objectMapper;

    JacksonBodyHandler(JavaType typeReference, ObjectMapper objectMapper) {
        this.typeReference = typeReference;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), this::toType);
    }

    public Type getType() {
        return typeReference;
    }

    private T toType(InputStream inputStream) {
        try (InputStream stream = inputStream) {
            String text = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}