package se.arkalix.query;

/**
 * Signifies that some service queried for could not be resolved.
 */
public class ServiceNotFoundException extends Exception {
    /**
     * Creates new exception from given failing service {@code query}.
     *
     * @param query {@link ServiceQuery} that could not be resolved.
     */
    public ServiceNotFoundException(final ServiceQuery query) {
        super(queryToString(query));
    }

    private static String queryToString(final ServiceQuery query) {
        final var builder = new StringBuilder(
            "No service with the following properties could be resolved: ");

        builder
            .append("name=")
            .append(query.name())
            .append(", isSecure=")
            .append(query.isSecure());

        final var codecs = query.codecTypes();
        if (codecs.size() > 0) {
            builder.append(", codecs=[");
            var isFirst = true;
            for (final var codec : codecs) {
                if (!isFirst) {
                    builder.append(", ");
                }
                isFirst = false;
                builder.append(codec.name());
            }
            builder.append(']');
        }

        final var protocolTypes = query.protocolTypes();
        if (protocolTypes.size() > 0) {
            builder.append(", protocolTypes=[");
            var isFirst = true;
            for (final var protocolType : protocolTypes) {
                if (!isFirst) {
                    builder.append(", ");
                }
                isFirst = false;
                builder.append(protocolType.name());
            }
            builder.append(']');
        }

        query.version().ifPresent(version -> builder
            .append(", version=")
            .append(version));

        query.versionMax().ifPresent(versionMax -> builder
            .append(", versionMax=")
            .append(versionMax));

        query.versionMin().ifPresent(versionMin -> builder
            .append(", versionMin=")
            .append(versionMin));

        return builder.toString();
    }
}
