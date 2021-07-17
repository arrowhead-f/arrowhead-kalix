package se.arkalix.util.logging;

import se.arkalix.util.logging._internal.TextLogger;

public final class Loggers {
    private Loggers() {}

    public static Logger text() {
        return TextLogger.instance();
    }
}
