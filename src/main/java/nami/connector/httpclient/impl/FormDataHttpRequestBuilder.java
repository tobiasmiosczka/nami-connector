package nami.connector.httpclient.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FormDataHttpRequestBuilder {

    private final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
            .setHeader("content-type", "application/x-www-form-urlencoded");

    private final Map<String, String> data = new HashMap<>();

    private static HttpRequest.BodyPublisher build(final Map<String, String> data) {
        String s = data.entrySet().stream()
                .map(e -> encode(e.getKey(), UTF_8) + "=" + encode(e.getValue(), UTF_8))
                .reduce(((s1, s2) -> s1 + "&" + s2))
                .orElse("");
        return HttpRequest.BodyPublishers.ofString(s);
    }

    public FormDataHttpRequestBuilder withValue(final String key, final String value) {
        data.put(key, value);
        return this;
    }

    public FormDataHttpRequestBuilder uri(final URI uri) {
        httpRequestBuilder.uri(uri);
        return this;
    }

    public HttpRequest build() {
        return httpRequestBuilder
                .POST(build(data))
                .build();
    }
}
