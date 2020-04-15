package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.ServiceQueryDto;
import se.arkalix.core.plugin.dto.ServiceQueryResultDto;
import se.arkalix.core.plugin.dto.ServiceRegistrationDto;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.consumer.HttpConsumer;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.util.concurrent.Future;

import java.util.Collections;
import java.util.Objects;

import static se.arkalix.core.plugin.HttpJsonServices.unwrap;
import static se.arkalix.net.http.HttpMethod.DELETE;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArServiceDiscovery} service that is communicated with via
 * HTTP/JSON in either secure or insecure mode.
 */
public class HttpJsonServiceDiscovery implements ArServiceDiscovery {
    private final HttpConsumer consumer;
    private final ServiceDescription service;

    private final String uriQuery;
    private final String uriRegister;
    private final String uriUnregister;

    public HttpJsonServiceDiscovery(final HttpClient client, final ServiceDescription service) {
        Objects.requireNonNull(client, "Expected client");
        this.service = Objects.requireNonNull(service, "Expected service");

        consumer = new HttpConsumer(client, service, Collections.singleton(EncodingDescriptor.JSON));

        final var basePath = service.uri();
        uriQuery = basePath + "/query";
        uriRegister = basePath + "/register";
        uriUnregister = basePath + "/unregister";
    }

    @Override
    public ServiceDescription service() {
        return service;
    }

    @Override
    public Future<ServiceQueryResultDto> query(final ServiceQueryDto query) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .uri(uriQuery)
                .body(query))
            .flatMap(response -> unwrap(response, ServiceQueryResultDto.class));
    }

    @Override
    public Future<?> register(final ServiceRegistrationDto registration) {
        return consumer
            .send(new HttpConsumerRequest()
                .method(POST)
                .uri(uriRegister)
                .body(registration))
            .flatMap(response -> unwrap(response, null));
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
                .uri(uriUnregister)
                .queryParameter("service_definition", serviceName)
                .queryParameter("system_name", systemName)
                .queryParameter("address", hostname)
                .queryParameter("port", Integer.toString(port)))
            .flatMap(response -> unwrap(response, null));
    }
}
