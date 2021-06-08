package se.arkalix.core.plugin.or;

import se.arkalix.ArConsumer;
import se.arkalix.ServiceRecord;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

import static se.arkalix.core.plugin._internal.HttpJsonServices.unwrap;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArOrchestrationService} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonOrchestrationService implements ArConsumer, ArOrchestrationService {
    private final HttpConsumer consumer;

    public HttpJsonOrchestrationService(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public ServiceRecord service() {
        return consumer.service();
    }

    @Override
    public Future<OrchestrationQueryResultDto> query(final OrchestrationQueryDto query) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .path(service().uri())
                .body(query::encodeJson))
            .flatMap(response -> unwrap(response, OrchestrationQueryResultDto::decodeJson));
    }
}
