package se.arkalix.core.plugin.eh;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.codec.CodecType;
import se.arkalix.net.ProtocolType;
import se.arkalix.core.plugin._internal.HttpJsonServices;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static se.arkalix.codec.CodecType.JSON;
import static se.arkalix.net.ProtocolType.HTTP;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArEventSubscribeService} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
@SuppressWarnings("unused")
public class HttpJsonEventSubscribeService implements ArConsumer, ArEventSubscribeService {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;

    private HttpJsonEventSubscribeService(final HttpConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonEventSubscribeService> factory() {
        return factory;
    }

    @Override
    public Future<?> subscribe(final EventSubscriptionRequestDto subscription) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .path(service().uri())
            .body(subscription::encodeJson))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public ServiceRecord service() {
        return consumer.service();
    }

    private static class Factory implements ArConsumerFactory<HttpJsonEventSubscribeService> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("event-subscribe");
        }

        @Override
        public Collection<ProtocolType> serviceProtocolTypes() {
            return Collections.singleton(HTTP);
        }

        @Override
        public Collection<CodecType> serviceCodecTypes() {
            return Collections.singleton(JSON);
        }

        @Override
        public HttpJsonEventSubscribeService create(
            final ArSystem system,
            final ServiceRecord service,
            final Collection<CodecType> codecTypes
        ) {
            return new HttpJsonEventSubscribeService(HttpConsumer.create(system, service, codecTypes));
        }
    }
}
