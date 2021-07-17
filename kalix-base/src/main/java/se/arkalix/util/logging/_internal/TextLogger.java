package se.arkalix.util.logging._internal;

import se.arkalix.util.annotation.Internal;
import se.arkalix.util.logging.Event;
import se.arkalix.util.logging.Logger;

@Internal
public final class TextLogger implements Logger {
    private static final TextLogger INSTANCE = new TextLogger();

    private TextLogger() {}

    public static Logger instance() {
        return INSTANCE;
    }

    @Override
    public void log(final Event event) {
        System.err.println(event.timestamp() + " " + event.name() + ": " + event.attributes());
    }
}
