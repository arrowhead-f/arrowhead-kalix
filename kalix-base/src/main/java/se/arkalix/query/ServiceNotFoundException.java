package se.arkalix.query;

public class ServiceNotFoundException extends Exception {
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

        final var encodings = query.encodings();
        if (encodings.size() > 0) {
            builder.append(", encodings=[");
            var isFirst = true;
            for (final var encoding : encodings) {
                if (!isFirst) {
                    builder.append(", ");
                }
                isFirst = false;
                builder.append(encoding.name());
            }
            builder.append(']');
        }

        final var transports = query.transports();
        if (transports.size() > 0) {
            builder.append(", transports=[");
            var isFirst = true;
            for (final var transport : transports) {
                if (!isFirst) {
                    builder.append(", ");
                }
                isFirst = false;
                builder.append(transport.name());
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
