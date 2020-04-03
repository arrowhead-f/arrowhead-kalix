package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.OrchestrationQueryDto;
import se.arkalix.core.plugin.dto.OrchestrationQueryResultDto;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponseException;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

import static se.arkalix.dto.DtoEncoding.JSON;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArOrchestration} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonOrchestration implements ArOrchestration {
    private final HttpClient client;
    private final InetSocketAddress remoteSocketAddress;

    private final String uriQuery;

    private HttpJsonOrchestration(final Builder builder) {
        client = Objects.requireNonNull(builder.client, "Expected client");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");

        final var basePath = Objects.requireNonNullElse(builder.basePath, "/orchestrator");
        uriQuery = basePath + "/orchestration";
    }

    @Override
    public Future<OrchestrationQueryResultDto> query(final OrchestrationQueryDto query) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(POST)
                .uri(uriQuery)
                .body(JSON, query))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString() // TODO: Parse error message and present better string.
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Orchestration query failed"));
            });
    }

    /**
     * Builder useful for constructing {@link HttpJsonServiceDiscovery} instances.
     */
    public static class Builder {
        private String basePath;
        private HttpClient client;
        private InetSocketAddress remoteSocketAddress;

        /**
         * Orchestration service base path.
         * <p>
         * Defaults to "/orchestrator".
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
         * Finishes construction of {@link HttpJsonOrchestration}.
         *
         * @return New HTTP/JSON orchestration service object.
         */
        public HttpJsonOrchestration build() {
            return new HttpJsonOrchestration(this);
        }
    }
}
