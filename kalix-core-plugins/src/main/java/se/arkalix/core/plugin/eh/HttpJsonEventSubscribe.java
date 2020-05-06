package se.arkalix.core.plugin.eh;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static se.arkalix.internal.core.plugin.HttpJsonServices.unwrap;
import static se.arkalix.descriptor.EncodingDescriptor.JSON;
import static se.arkalix.descriptor.TransportDescriptor.HTTP;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArEventSubscribe} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
@SuppressWarnings("unused")
public class HttpJsonEventSubscribe implements ArConsumer, ArEventSubscribe {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;

    private HttpJsonEventSubscribe(final HttpConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonEventSubscribe> factory() {
        return factory;
    }

    @Override
    public Future<?> subscribe(final EventSubscriptionRequestDto subscription) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(service().uri())
            .body(subscription))
            .flatMap(response -> unwrap(response, null));
    }

    @Override
    public ServiceDescription service() {
        return consumer.service();
    }

    private static class Factory implements ArConsumerFactory<HttpJsonEventSubscribe> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("event-subscribe");
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
        public HttpJsonEventSubscribe create(
            final ArSystem system,
            final ServiceDescription service,
            final Collection<EncodingDescriptor> encodings) throws Exception
        {
            return new HttpJsonEventSubscribe(new HttpConsumer(HttpClient.from(system), service, encodings));
        }
    }
}
