package se.arkalix.net;

import se.arkalix.encoding.Encodable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Description of how to assemble the body to be sent with some outgoing
 * message.
 */
public interface BodyOutgoing {
    /**
     * Creates new outgoing message body to contain the given {@code encodable}
     * object.
     *
     * @param encodable Object to use as message body.
     * @return Created body.
     */
    static BodyOutgoing create(final Encodable encodable) {
        Objects.requireNonNull(encodable, "encodable");
        return new BodyOutgoing() {
            @Override
            public Optional<Encodable> encodable() {
                return Optional.of(encodable);
            }

            @Override
            public Optional<Path> path() {
                return Optional.empty();
            }
        };
    }

    /**
     * Creates new outgoing message body to contain the file at the given
     * {@code path}.
     *
     * @param path Path to file to use as message body.
     * @return Created body.
     */
    static BodyOutgoing create(final Path path) {
        Objects.requireNonNull(path, "path");
        return new BodyOutgoing() {
            @Override
            public Optional<Encodable> encodable() {
                return Optional.empty();
            }

            @Override
            public Optional<Path> path() {
                return Optional.of(path);
            }
        };
    }

    /**
     * Gets {@link Encodable} set as outgoing message body, if any.
     *
     * @return Set encodable, if any.
     */
    Optional<Encodable> encodable();

    /**
     * Gets {@link Path} pointing to file set as outgoing message body, if any.
     *
     * @return Set path, if any.
     */
    Optional<Path> path();
}
