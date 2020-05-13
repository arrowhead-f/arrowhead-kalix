package se.arkalix.core.plugin.cp;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.internal.core.plugin.HttpJsonServices;
import se.arkalix.internal.core.plugin.Paths;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static se.arkalix.descriptor.EncodingDescriptor.JSON;
import static se.arkalix.descriptor.TransportDescriptor.HTTP;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArContractNegotiationTrusted} service that is communicated
 * with via HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonContractNegotiationTrusted implements ArConsumer, ArContractNegotiationTrusted {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;
    private final String uriAccept;
    private final String uriOffer;
    private final String uriReject;

    private HttpJsonContractNegotiationTrusted(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
        final var basePath = consumer.service().uri();
        uriAccept = Paths.combine(basePath, "acceptances");
        uriOffer = Paths.combine(basePath, "offers");
        uriReject = Paths.combine(basePath, "rejections");
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonContractNegotiationTrusted> factory() {
        return factory;
    }

    @Override
    public ServiceDescription service() {
        return consumer.service();
    }

    @Override
    public Future<?> accept(final TrustedAcceptanceDto acceptance) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriAccept)
            .body(acceptance))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public Future<?> offer(final TrustedOfferDto offer) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriOffer)
            .body(offer))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public Future<?> reject(final TrustedRejectionDto rejection) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriReject)
            .body(rejection))
            .flatMap(HttpJsonServices::unwrap);
    }

    private static class Factory implements ArConsumerFactory<HttpJsonContractNegotiationTrusted> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("contract-negotiation-trusted");
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
        public HttpJsonContractNegotiationTrusted create(
            final ArSystem system,
            final ServiceDescription service,
            final Collection<EncodingDescriptor> encodings) throws Exception
        {
            final var consumer = new HttpConsumer(HttpClient.from(system), service, encodings);
            return new HttpJsonContractNegotiationTrusted(consumer);
        }
    }
}