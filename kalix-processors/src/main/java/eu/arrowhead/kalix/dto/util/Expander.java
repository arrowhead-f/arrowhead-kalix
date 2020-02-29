package eu.arrowhead.kalix.dto.util;

@FunctionalInterface
public interface Expander {
    String expand(String x);
}
