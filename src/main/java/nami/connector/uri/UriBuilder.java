package nami.connector.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class UriBuilder {

    private String scheme;
    private String userInfo;
    private String host;
    private int port = -1;
    private final List<String> path = new ArrayList<>();
    private final Map<String, String> parameters = new HashMap<>();
    private String fragment;

    private static List<String> splitPath(String path) {
        if (isNull(path)) {
            return List.of();
        }
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static Map<String, String> splitParameters(String query) {
        if (isNull(query)) {
            return Map.of();
        }
        return Arrays.stream(query.split("&"))
                .filter(e -> !e.isEmpty())
                .map(e -> {
                    String[] split = e.split("=", 2);
                    if (split.length == 2) {
                        return Map.entry(split[0], split[1]);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static UriBuilder fromUri(final URI uri) {
        return create()
                .withScheme(uri.getScheme())
                .withUserInfo(uri.getUserInfo())
                .withHost(uri.getHost())
                .withPort(uri.getPort())
                .withPath(splitPath(uri.getPath()))
                .withParameters(splitParameters(uri.getQuery()))
                .withFragment(uri.getFragment());
    }

    public static UriBuilder create() {
        return new UriBuilder();
    }

    private UriBuilder() {

    }

    public UriBuilder copy() {
        return create()
                .withScheme(scheme)
                .withUserInfo(userInfo)
                .withHost(host)
                .withPort(port)
                .withPath(path)
                .withParameters(parameters)
                .withFragment(fragment);
    }

    public UriBuilder withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public UriBuilder withUserInfo(String userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public UriBuilder withPath(String path) {
        return withPath(splitPath(path));
    }

    public UriBuilder withPath(Collection<String> paths) {
        this.path.clear();
        this.path.addAll(paths);
        return this;
    }

    public UriBuilder appendPath(int pathAppendix) {
        return appendPath(Integer.toString(pathAppendix));
    }

    public UriBuilder appendPath(String path) {
        this.path.addAll(splitPath(path));
        return this;
    }

    public UriBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public UriBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    public UriBuilder withParameter(String key, int value) {
        return this.withParameter(key, Integer.toString(value));
    }

    public UriBuilder withParameter(String key, String value) {
        parameters.put(key, value);
        return this;
    }

    public UriBuilder withParameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    public UriBuilder withFragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    public URI build() {
        try {
            return new URI(scheme, userInfo, host, port, buildPath(), buildQuery(), fragment);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildPath() {
        if (path.isEmpty()) {
            return "";
        }
        return "/" + String.join("/", path);
    }

    private String buildQuery() {
        if (parameters.isEmpty()) {
            return null;
        }
        return parameters.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).orElse("");
    }
}
