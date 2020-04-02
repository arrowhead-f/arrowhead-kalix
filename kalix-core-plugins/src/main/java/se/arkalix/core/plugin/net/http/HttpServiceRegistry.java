package se.arkalix.core.plugin.net.http;

import se.arkalix.core.plugin.ArServiceRegistry;
import se.arkalix.core.plugin.dto.ServiceQueryDto;
import se.arkalix.core.plugin.dto.ServiceQueryResultDto;
import se.arkalix.core.plugin.dto.ServiceRegistrationDto;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponseException;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

import static se.arkalix.net.http.HttpMethod.*;

/**
 * A remote {@link ArServiceRegistry} that is communicated with via HTTP.
 */
public class HttpServiceRegistry implements ArServiceRegistry {
   /* static {
        ArConsumer.Registry.set(HttpServiceRegistry.class, new Factory() {
            @Override
            public ServiceQuery createQuery(final ArSystem system, final ArServiceResolver resolver) {
                return new ServiceQuery("servicediscovery", system.isSecure(), resolver)
                    .encodings(EncodingDescriptor.dtoEncodings())
                    .transports(HTTP);
            }

            @Override
            public ArConsumer createInstance(
                final ArSystem system,
                final ServiceDescription description) throws Exception
            {
                final var encoding = description.interfaces().stream()
                    .filter(i -> i.transport() == HTTP && i.encoding().isDtoEncoding())
                    .findFirst()
                    .flatMap(triplet -> triplet.encoding().asDtoEncoding())
                    .orElseThrow(() -> {
                        final var encodings = EncodingDescriptor.dtoEncodings();
                        return new IllegalStateException("When creating " +
                            "service query the following encodings were " +
                            "stated as supported: " + encodings + ", but " +
                            "none of them are in the provided service " +
                            "description; cannot create HttpServiceRegistry");
                    });

                return new Builder()
                    .basePath(description.uri())
                    .client(HttpClient.from(system))
                    .encoding(encoding)
                    .remoteSocketAddress(description.provider().remoteSocketAddress())
                    .build();
            }
        });
    }*/

    private final HttpClient client;
    private final DtoEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    private final String uriQuery;
    private final String uriRegister;
    private final String uriUnregister;

    private HttpServiceRegistry(final Builder builder) {
        client = Objects.requireNonNull(builder.client, "Expected client");
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");

        final var basePath = Objects.requireNonNullElse(builder.basePath, "/serviceregistry");
        uriQuery = basePath + "/query";
        uriRegister = basePath + "/register";
        uriUnregister = basePath + "/unregister";
    }

    @Override
    public Future<ServiceQueryResultDto> query(final ServiceQueryDto query) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(POST)
                .uri(uriQuery)
                .body(encoding, query))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString()
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to query " +
                    "service registry for \"" + query.name() + "\""));
            });
    }

    @Override
    public Future<?> register(final ServiceRegistrationDto registration) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(POST)
                .uri(uriRegister)
                .body(encoding, registration))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString()
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to register service \"" + registration.name() + "\""));
            });
    }

    @Override
    public Future<?> unregister(
        final String serviceName,
        final String systemName,
        final InetSocketAddress systemSocketAddress)
    {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(DELETE)
                .uri(uriUnregister)
                .queryParameter("service_definition", serviceName)
                .queryParameter("system_name", systemName)
                .queryParameter("address", systemSocketAddress.getHostString())
                .queryParameter("port", Integer.toString(systemSocketAddress.getPort())))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString()
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to unregister service \"" + serviceName + "\""));
            });
    }

    /**
     * Builder useful for constructing {@link HttpServiceRegistry} instances.
     */
    public static class Builder {
        private String basePath;
        private HttpClient client;
        private DtoEncoding encoding;
        private InetSocketAddress remoteSocketAddress;

        /**
         * Service registry base path.
         * <p>
         * Defaults to "/serviceregistry".
         *
         * @param basePath Base path.
         * @return This builder.
         */
        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        /**
         * HTTP client to use for making requests. <b>Must be specified.</b>
         *
         * @param client HTTP client.
         * @return This builder.
         */
        public Builder client(final HttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Encoding that must be used to encode and decode messages sent to and
         * received from the service registry. <b>Must be specified.</b>
         *
         * @param encoding HTTP body encoding.
         * @return This builder.
         */
        public Builder encoding(final DtoEncoding encoding) {
            this.encoding = encoding;
            return this;
        }

        /**
         * The hostname/port of the service registry. <b>Must be specified.</b>
         *
         * @param remoteSocketAddress Hostname/port.
         * @return This builder.
         */
        public Builder remoteSocketAddress(final InetSocketAddress remoteSocketAddress) {
            this.remoteSocketAddress = remoteSocketAddress;
            return this;
        }

        /**
         * Finishes construction of {@link HttpServiceRegistry}.
         *
         * @return New HTTP service registry object.
         */
        public HttpServiceRegistry build() {
            return new HttpServiceRegistry(this);
        }
    }
}
