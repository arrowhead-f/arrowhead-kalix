package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.Error;
import se.arkalix.core.plugin.dto.ErrorDto;
import se.arkalix.core.plugin.dto.OrchestrationQueryDto;
import se.arkalix.core.plugin.dto.OrchestrationQueryResultDto;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collections;
import java.util.Objects;

import static se.arkalix.dto.DtoEncoding.JSON;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArOrchestration} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonOrchestration implements ArOrchestration {
    private final HttpConsumer consumer;
    private final ServiceDescription service;

    private final String uriQuery;

    /**
     * Creates new HTTP/JSON orchestration service consumer.
     *
     * @param client  HTTP client to use when communicating with service
     *                provider.
     * @param service Description of orchestration service to consume.
     * @throws NullPointerException     If {@code client} or {@code service} is
     *                                  {@code null}.
     * @throws IllegalArgumentException If the name of given {@code service} is
     *                                  not {@code "orchestration"} or {@code
     *                                  client} is configured to use HTTPS and
     *                                  the {@link
     *                                  ServiceDescription#security()} method
     *                                  of the given {@code service} returns
     *                                  {@link SecurityDescriptor#NOT_SECURE},
     *                                  or if {@code client} is configured to
     *                                  use plain HTTP and the given {@code
     *                                  service} is secure returns any other
     *                                  security/access policy than
     *                                  {@link SecurityDescriptor#NOT_SECURE}.
     * @throws IllegalStateException    If {@code service} does not support any
     *                                  interface triplet compatible with the
     *                                  given {@code client} and any one out of
     *                                  the given {@code encodings}.
     */
    public HttpJsonOrchestration(final HttpClient client, final ServiceDescription service) {
        Objects.requireNonNull(client, "Expected client");
        this.service = Objects.requireNonNull(service, "Expected service");

        if (!Objects.equals(service.name(), "orchestration-service")) {
            throw new IllegalArgumentException("Expected given service to " +
                "have the name \"orchestration-service\", but its name is \"" +
                service.name() + "\"; cannot create HTTP/JSON orchestration " +
                "service consumer");
        }

        consumer = new HttpConsumer(client, service, Collections.singleton(EncodingDescriptor.JSON));
        // Service URI ignored as it seems to tend to be incorrect.
        // See https://github.com/arrowhead-f/core-java-spring/issues/195.
        uriQuery = "/orchestrator/orchestration";
    }

    @Override
    public ServiceDescription service() {
        return service;
    }

    @Override
    public Future<OrchestrationQueryResultDto> query(final OrchestrationQueryDto query) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .uri(uriQuery)
                .body(query))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return response.bodyAsClassIfSuccess(OrchestrationQueryResultDto.class);
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAs(JSON, ErrorDto.class)
                        .mapThrow(Error::toException);
                }
                return Future.failure(response.reject("Orchestration query failed"));
            });
    }
}
