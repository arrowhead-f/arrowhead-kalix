package se.arkalix.core.plugin;

import se.arkalix.ArConsumer;
import se.arkalix.core.plugin.dto.OrchestrationQueryDto;
import se.arkalix.core.plugin.dto.OrchestrationQueryResultDto;
import se.arkalix.description.ServiceDescription;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

import static se.arkalix.core.plugin.internal.HttpJsonServices.unwrap;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArOrchestration} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonOrchestration implements ArConsumer, ArOrchestration {
    private final HttpConsumer consumer;

    HttpJsonOrchestration(final HttpConsumer consumer) {
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
