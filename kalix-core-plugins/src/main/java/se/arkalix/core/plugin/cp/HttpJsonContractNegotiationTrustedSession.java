package se.arkalix.core.plugin.cp;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
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
import static se.arkalix.internal.core.plugin.HttpJsonServices.unwrapOptional;
import static se.arkalix.net.http.HttpMethod.GET;

/**
 * A remote {@link HttpJsonContractNegotiationTrustedSession} service that is
 * communicated with via HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonContractNegotiationTrustedSession implements ArConsumer, ArContractNegotiationTrustedSession {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;
    private final String uriGet;

    private HttpJsonContractNegotiationTrustedSession(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
        uriGet = Paths.combine(consumer.service().uri(), "sessions");
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonContractNegotiationTrustedSession> factory() {
        return factory;
    }

    @Override
    public ServiceDescription service() {
        return consumer.service();
    }

    @Override
    public Future<Optional<TrustedSessionDto>> getByNamesAndId(
        final String name1,
        final String name2,
        final long id)
    {
        return consumer.send(new HttpConsumerRequest()
            .method(GET)
            .uri(uriGet)
            .queryParameter("name1", name1)
            .queryParameter("name2", name2)
            .queryParameter("id", "" + id))
            .flatMap(response -> unwrapOptional(response, TrustedSessionDto.class));
    }

    private static class Factory implements ArConsumerFactory<HttpJsonContractNegotiationTrustedSession> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("contract-negotiation-trusted-session");
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
        public HttpJsonContractNegotiationTrustedSession create(
            final ArSystem system,
            final ServiceDescription service,
            final Collection<EncodingDescriptor> encodings) throws Exception
        {
            final var consumer = new HttpConsumer(HttpClient.from(system), service, encodings);
            return new HttpJsonContractNegotiationTrustedSession(consumer);
        }
    }
}