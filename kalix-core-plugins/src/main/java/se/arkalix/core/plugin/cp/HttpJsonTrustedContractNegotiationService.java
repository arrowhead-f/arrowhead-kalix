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
 * A remote {@link ArTrustedContractNegotiationService} service that is communicated
 * with via HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonTrustedContractNegotiationService implements ArConsumer, ArTrustedContractNegotiationService {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;
    private final String uriAccept;
    private final String uriCounterOffer;
    private final String uriOffer;
    private final String uriReject;

    private HttpJsonTrustedContractNegotiationService(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
        final var basePath = consumer.service().uri();
        uriAccept = Paths.combine(basePath, "acceptances");
        uriCounterOffer = Paths.combine(basePath, "counter-offers");
        uriOffer = Paths.combine(basePath, "offers");
        uriReject = Paths.combine(basePath, "rejections");
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonTrustedContractNegotiationService> factory() {
        return factory;
    }

    @Override
    public ServiceDescription service() {
        return consumer.service();
    }

    @Override
    public Future<?> accept(final TrustedContractAcceptanceDto acceptance) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriAccept)
            .body(acceptance))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public Future<Long> offer(final TrustedContractOfferDto offer) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriOffer)
            .body(offer))
            .flatMapResult(result -> {
                if (result.isFailure()) {
                    return Future.failure(result.fault());
                }
                final var response = result.value();
                final var optionalLocation = response.header("location");
                if (optionalLocation.isEmpty()) {
                    return Future.failure(response.reject("No location " +
                        "header in response; cannot determine session id"));
                }
                final var location = optionalLocation.get();
                final var idOffset = location.lastIndexOf('/', location.length() - 2);
                if (idOffset == -1) {
                    return Future.failure(response.reject("No valid URI in " +
                        "location header; cannot determine session id"));
                }
                final long negotiationId;
                try {
                    negotiationId = Long.parseLong(location, idOffset, location.length(), 10);
                }
                catch (final NumberFormatException exception) {
                    return Future.failure(response.reject("Last segment of " +
                        "location header does not contain a number; cannot " +
                        "determine session id"));
                }
                return HttpJsonServices.unwrap(response)
                    .pass(negotiationId);
            });
    }

    @Override
    public Future<?> counterOffer(final TrustedContractCounterOfferDto counterOffer) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriCounterOffer)
            .body(counterOffer))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public Future<?> reject(final TrustedContractRejectionDto rejection) {
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .uri(uriReject)
            .body(rejection))
            .flatMap(HttpJsonServices::unwrap);
    }

    private static class Factory implements ArConsumerFactory<HttpJsonTrustedContractNegotiationService> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("trusted-contract-negotiation");
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
        public HttpJsonTrustedContractNegotiationService create(
            final ArSystem system,
            final ServiceDescription service,
            final Collection<EncodingDescriptor> encodings) throws Exception
        {
            final var consumer = new HttpConsumer(HttpClient.from(system), service, encodings);
            return new HttpJsonTrustedContractNegotiationService(consumer);
        }
    }
}