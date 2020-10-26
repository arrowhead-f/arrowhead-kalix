package se.arkalix.core.plugin.sr;

import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.core.plugin._internal.HttpJsonServices;
import se.arkalix.net.Encoding;
import se.arkalix.net.Uris;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collections;
import java.util.Objects;

import static se.arkalix.core.plugin._internal.HttpJsonServices.unwrap;
import static se.arkalix.net.http.HttpMethod.DELETE;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArServiceDiscoveryService} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonServiceDiscoveryService implements ArServiceDiscoveryService {
    private final HttpConsumer consumer;

    private final String pathQuery;
    private final String pathRegister;
    private final String pathUnregister;

    public HttpJsonServiceDiscoveryService(final ArSystem system, final ServiceRecord service) {
        Objects.requireNonNull(system, "Expected system");
        Objects.requireNonNull(service, "Expected service");

        consumer = HttpConsumer.create(system, service, Collections.singleton(Encoding.JSON));

        final var basePath = service.uri();
        pathQuery = Uris.pathOf(basePath, "query");
        pathRegister = Uris.pathOf(basePath, "register");
        pathUnregister = Uris.pathOf(basePath, "unregister");
    }

    @Override
    public ServiceRecord service() {
        return consumer.service();
    }

    @Override
    public Future<ServiceQueryResultDto> query(final ServiceQueryDto query) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .path(pathQuery)
                .body(query))
            .flatMap(response -> unwrap(response, ServiceQueryResultDto.class));
    }

    @Override
    public Future<?> register(final ServiceRegistrationDto registration) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .path(pathRegister)
                .body(registration))
            .flatMap(HttpJsonServices::unwrap);
    }

    @Override
    public Future<?> unregister(
        final String serviceName,
        final String systemName,
        final String hostname,
        final int port)
    {
        return consumer
            .send(new HttpConsumerRequest()
                .method(DELETE)
                .path(pathUnregister)
                .queryParameter("service_definition", serviceName)
                .queryParameter("system_name", systemName)
                .queryParameter("address", hostname)
                .queryParameter("port", Integer.toString(port)))
            .flatMap(HttpJsonServices::unwrap);
    }
}
