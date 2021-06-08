package se.arkalix.core.plugin.cp;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.codec.CodecType;
import se.arkalix.net.ProtocolType;
import se.arkalix.net.Uris;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static se.arkalix.core.plugin._internal.HttpJsonServices.unwrapOptional;
import static se.arkalix.codec.CodecType.JSON;
import static se.arkalix.net.ProtocolType.HTTP;
import static se.arkalix.net.http.HttpMethod.GET;

/**
 * A remote {@link HttpJsonTrustedContractObservationService} service that is
 * communicated with via HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonTrustedContractObservationService implements ArConsumer, ArTrustedContractObservationService {
    private static final Factory factory = new Factory();

    private final HttpConsumer consumer;
    private final String uriGet;

    private HttpJsonTrustedContractObservationService(final HttpConsumer consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        uriGet = Uris.pathOf(consumer.service().uri(), "negotiations");
    }

    /**
     * @return Consumer {@link ArConsumerFactory factory class}.
     */
    public static ArConsumerFactory<HttpJsonTrustedContractObservationService> factory() {
        return factory;
    }

    @Override
    public ServiceRecord service() {
        return consumer.service();
    }

    @Override
    public Future<Optional<TrustedContractNegotiationDto>> getByNamesAndId(
        final String name1,
        final String name2,
        final long id)
    {
        return consumer.send(new HttpConsumerRequest()
            .method(GET)
            .path(uriGet)
            .queryParameter("name1", name1)
            .queryParameter("name2", name2)
            .queryParameter("id", "" + id))
            .flatMap(response -> unwrapOptional(response, TrustedContractNegotiationDto::decodeJson));
    }

    private static class Factory implements ArConsumerFactory<HttpJsonTrustedContractObservationService> {
        @Override
        public Optional<String> serviceName() {
            return Optional.of("trusted-contract-observation");
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
        public HttpJsonTrustedContractObservationService create(
            final ArSystem system,
            final ServiceRecord service,
            final Collection<CodecType> codecTypes
        ) {
            final var consumer = HttpConsumer.create(system, service, codecTypes);
            return new HttpJsonTrustedContractObservationService(consumer);
        }
    }
}