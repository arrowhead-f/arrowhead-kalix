package se.arkalix.core.plugin.eh;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.internal.core.plugin.HttpJsonServices;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static se.arkalix.descriptor.EncodingDescriptor.JSON;
import static se.arkalix.descriptor.TransportDescriptor.HTTP;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArEventPublishService} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
@SuppressWarnings("unused")
public class HttpJsonEventPublishService implements ArConsumer, ArEventPublishService {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;

    private HttpJsonEventPublishService(final HttpConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonEventPublishService> factory() {
        return factory;
    }

    @Override
    public Future<?> publish(final EventOutgoingDto event) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .path(service().uri())
            .body(event))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public ServiceRecord service() {
        return consumer.service();
    }

    private static class Factory implements ArConsumerFactory<HttpJsonEventPublishService> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("event-publish");
        }

        @Override
        public Collection<TransportDescriptor> serviceTransports() {
            return Collections.singleton(HTTP);
        }

        @Override
        public Collection<EncodingDescriptor> serviceEncodings() {
            return Collections.singleton(JSON);
        }

        @Override
        public HttpJsonEventPublishService create(
            final ArSystem system,
            final ServiceRecord service,
            final Collection<EncodingDescriptor> encodings
        ) {
            return new HttpJsonEventPublishService(HttpConsumer.create(system, service, encodings));
        }
    }
}
