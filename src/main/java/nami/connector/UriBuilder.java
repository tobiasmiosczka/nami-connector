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

    public UriBuilder appendPath(String pathAppendix) {
        String path = getPath();
        if (path.isEmpty())
            path = "/";
        if ((path.charAt(path.length() - 1) != '/') && (pathAppendix.charAt(0) != '/'))
            setPath(path + "/" + pathAppendix);
        else
            setPath(path + pathAppendix);
        return this;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public UriBuilder setParameter(String key, int value) {
        return this.setParameter(key, Integer.toString(value));
    }

    public UriBuilder setParameter(String key, String value) {
        params.put(encode(key), encode(value));
        return this;
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public URI build() {
        StringBuilder stringBuilder = new StringBuilder()
                .append(scheme)
                .append("://")
                .append(host)
                .append(path);
        String paramString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((s1, s2) -> s1 + "&" + s2).orElse("");
        if (!paramString.isEmpty())
            paramString = "?" + paramString;
        stringBuilder.append(paramString);
        return URI.create(stringBuilder.toString());
    }
}
