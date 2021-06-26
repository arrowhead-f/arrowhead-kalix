package se.arkalix.util.logging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event {
    private final ArrayList<Attribute> attributes;

    public Event() {
        this(10);
    }

    public Event(final int initialCapacity) {
        this.attributes = new ArrayList<>(initialCapacity);
    }

    public Event with(final String label, final Object value) {
        final String value0;
        if (value != null) {
            value0 = value.toString();
        } else {
            value0 = null;
        }
        attributes.add(new Attribute(label, value0));
        return this;
    }

    public Event withException(final Throwable value) {
        attributes.add(new Attribute("exception", value));
        return this;
    }

    public Event withMessage(final String value) {
        attributes.add(new Attribute("message", value));
        return this;
    }

    public Event withTimestamp() {
        return withTimestamp(Instant.now());
    }

    public Event withTimestamp(final Instant value) {
        attributes.add(new Attribute("timestamp", value));
        return this;
    }

    public List<Attribute> attributes() {
        return Collections.unmodifiableList(attributes);
    }
}
