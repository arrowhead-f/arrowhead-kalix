package se.arkalix.internal.core.plugin;

import se.arkalix.util.annotation.Internal;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Various utilities for managing dates and times.
 */
@Internal
public class Instants {
    private Instants() {}

    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .toFormatter()
        .withZone(ZoneOffset.UTC);

    public static String toAitiaDateTimeString(final Instant instant) {
        return dateTimeFormatter.format(instant);
    }

    /**
     * Creates {@link Instant} from given date/time string formatted according
     * to the following pattern:
     * <pre>
     *     yyyy-MM-dd HH:mm:ss
     * </pre>
     *
     * @param dateTime String to parse.
     * @return Created {@link Instant}.
     * @throws java.time.format.DateTimeParseException If parsing fails.
     */
    public static Instant fromAitiaDateTimeString(final String dateTime) {
        return dateTimeFormatter.parse(dateTime, Instant::from);
    }
}
