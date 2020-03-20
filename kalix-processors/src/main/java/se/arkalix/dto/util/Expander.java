package se.arkalix.dto.util;

@FunctionalInterface
public interface Expander {
    String expand(String x);
}
