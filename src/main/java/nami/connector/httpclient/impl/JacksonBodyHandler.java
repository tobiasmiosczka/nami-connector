package nami.connector.httpclient.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;

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

    public T toType(InputStream inputStream) {
        try (InputStream stream = inputStream) {
            return objectMapper.readValue(stream, typeReference);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}