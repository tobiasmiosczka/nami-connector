package nami.connector.httpclient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpUtil {

    public static HttpRequest.BodyPublisher ofFormData(Map<String, String> data) {
        String s = data.entrySet().stream()
                .map(e -> encode(e.getKey(), UTF_8) + "=" + encode(e.getValue(), UTF_8))
                .reduce(((s1, s2) -> s1 + "&" + s2))
                .orElse("");
        return HttpRequest.BodyPublishers.ofString(s);
    }

    public static HttpRequest buildGetRequest(URI uri) {
        return HttpRequest.newBuilder().uri(uri).GET().build();
    }
}
