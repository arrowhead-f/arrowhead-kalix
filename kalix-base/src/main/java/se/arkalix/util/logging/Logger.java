package se.arkalix.util.logging;

@FunctionalInterface
public interface Logger {
    void log(Event event);
}
