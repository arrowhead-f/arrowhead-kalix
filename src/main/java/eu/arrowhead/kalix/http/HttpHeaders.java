package eu.arrowhead.kalix.http;

import java.util.*;

public class HttpHeaders {
    private final HashMap<String, String> map = new HashMap<>();

    public HttpHeaders() {}

    public HttpHeaders add(final String name, final String value) {
        Objects.requireNonNull(name);
        map.compute(name.toLowerCase(), (k, v) -> v == null ? value : v + "," + value);
        return this;
    }

    public Optional<String> get(final String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(map.get(name.toLowerCase()));
    }

    public HttpHeaders set(final String name, final String value) {
        Objects.requireNonNull(name);
        map.put(name.toLowerCase(), value);
        return this;
    }

    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
