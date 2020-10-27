package se.arkalix.core.plugin.cp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.core.plugin._internal.HttpJsonServices;
import se.arkalix.net.Encoding;
import se.arkalix.net.Transport;
import se.arkalix.net.Uris;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static se.arkalix.net.Encoding.JSON;
import static se.arkalix.net.Transport.HTTP;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArTrustedContractNegotiationService} service that is communicated
 * with via HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonTrustedContractNegotiationService implements ArConsumer, ArTrustedContractNegotiationService {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonTrustedContractNegotiationService.class);
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;
    private final String pathAccept;
    private final String pathCounterOffer;
    private final String pathOffer;
    private final String pathReject;

    private HttpJsonTrustedContractNegotiationService(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        final var basePath = consumer.service().uri();
        pathAccept = Uris.pathOf(basePath, "acceptances");
        pathCounterOffer = Uris.pathOf(basePath, "counter-offers");
        pathOffer = Uris.pathOf(basePath, "offers");
        pathReject = Uris.pathOf(basePath, "rejections");
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonTrustedContractNegotiationService> factory() {
        return factory;
    }

    @Override
    public ServiceRecord service() {
        return consumer.service();
    }

    @Override
    public Future<?> accept(final TrustedContractAcceptanceDto acceptance) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending {} to {}", acceptance, consumer.service());
        }
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .path(pathAccept)
            .body(acceptance))
            .flatMap(result -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent acceptance resulted in {}", result);
                }
                return HttpJsonServices.unwrap(result);
            });
    }

    @Override
    public Future<Long> offer(final TrustedContractOfferDto offer) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending {} to {}", offer, consumer.service());
        }
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .path(pathOffer)
            .body(offer))
            .flatMapResult(result -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent offer resulted in {}", result);
                }
                if (result.isFailure()) {
                    return Future.failure(result.fault());
                }
                final var response = result.value();
                if (!response.status().isSuccess()) {
                    return Future.failure(response.reject());
                }
                final var optionalLocation = response.header("location");
                if (optionalLocation.isEmpty()) {
                    return Future.failure(response.reject("No location " +
                        "header in response; cannot determine negotiation id"));
                }
                var location = optionalLocation.get();
                if (location.charAt(location.length() - 1) == '/') {
                    location = location.substring(0, location.length() - 1);
                }
                final var idOffset = location.lastIndexOf('/');
                if (idOffset == -1) {
                    return Future.failure(response.reject("No valid URI in " +
                        "location header; cannot determine negotiation id"));
                }
                final long negotiationId;
                try {
                    negotiationId = Long.parseLong(location, idOffset + 1, location.length(), 10);
                }
                catch (final NumberFormatException exception) {
                    return Future.failure(response.reject("Last segment of " +
                        "location header does not contain a number; cannot " +
                        "determine negotiation id", exception));
                }
                return HttpJsonServices.unwrap(response)
                    .pass(negotiationId);
            });
    }

    @Override
    public Future<?> counterOffer(final TrustedContractCounterOfferDto counterOffer) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending {} to {}", counterOffer, consumer.service());
        }
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .path(pathCounterOffer)
            .body(counterOffer))
            .flatMap(result -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent counter-offer resulted in {}", result);
                }
                return HttpJsonServices.unwrap(result);
            });
    }

    @Override
    public Future<?> reject(final TrustedContractRejectionDto rejection) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending {} to {}", rejection, consumer.service());
        }
        return consumer.send(new HttpConsumerRequest()
            .method(POST)
            .path(pathReject)
            .body(rejection))
            .flatMap(result -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent rejection resulted in {}", result);
                }
                return HttpJsonServices.unwrap(result);
            });
    }

    private static class Factory implements ArConsumerFactory<HttpJsonTrustedContractNegotiationService> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("trusted-contract-negotiation");
        }

        @Override
        public Collection<Transport> serviceTransports() {
            return Collections.singleton(HTTP);
        }

        @Override
        public Collection<Encoding> serviceEncodings() {
            return Collections.singleton(JSON);
        }

        @Override
        public HttpJsonTrustedContractNegotiationService create(
            final ArSystem system,
            final ServiceRecord service,
            final Collection<Encoding> encodings
        ) {
            final var consumer = HttpConsumer.create(system, service, encodings);
            return new HttpJsonTrustedContractNegotiationService(consumer);
        }
    }
}