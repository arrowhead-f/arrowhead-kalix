package se.arkalix.core.plugin.or;

import se.arkalix.ArConsumer;
import se.arkalix.description.ServiceDescription;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

import static se.arkalix.internal.core.plugin.HttpJsonServices.unwrap;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArOrchestrationService} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonOrchestrationService implements ArConsumer, ArOrchestrationService {
    private final HttpConsumer consumer;

    public HttpJsonOrchestrationService(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
    }

    @Override
    public ServiceDescription service() {
        return consumer.service();
    }

    @Override
    public Future<OrchestrationQueryResultDto> query(final OrchestrationQueryDto query) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .uri(service().uri())
                .body(query))
            .flatMap(response -> unwrap(response, OrchestrationQueryResultDto.class));
    }
}
