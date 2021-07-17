package se.arkalix.util.logging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Event {
    private final ArrayList<Attribute> attributes;
    private final String name;
    private final Instant timestamp;

    public Event(final String name) {
        this(name, 10);
    }

    public Event(final String name, final int initialCapacity) {
        this.attributes = new ArrayList<>(initialCapacity);
        this.name = Objects.requireNonNull(name, "name");
        timestamp = Instant.now();
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

    public Event withContext(final Class<?> context) {
        attributes.add(new Attribute("context", context));
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

    public List<Attribute> attributes() {
        return Collections.unmodifiableList(attributes);
    }

    public String name() {
        return name;
    }

    public Instant timestamp() {
        return timestamp;
    }
}
