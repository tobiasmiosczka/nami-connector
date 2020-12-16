package nami.connector;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UriBuilder {

    private final Map<String, String> params;
    private String scheme;
    private String path;
    private String host;

    public UriBuilder() {
        this.params = new HashMap<>();
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public UriBuilder setParameter(String key, String value) {
        params.put(key, value);
        return this;
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    protected URI build() {
        StringBuilder stringBuilder = new StringBuilder()
                .append(scheme)
                .append("://")
                .append(host)
                .append(path);
        String paramString = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).orElse("");
        if (!paramString.isEmpty())
            paramString = "?" + paramString;
        stringBuilder.append(paramString);
        return URI.create(stringBuilder.toString());
    }


}
