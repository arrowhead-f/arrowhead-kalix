package eu.arrowhead.kalix.core.plugins.sr;

import eu.arrowhead.kalix.core.plugins.sr.dto.*;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;

public class HttpServiceRegistry implements ServiceRegistry {
    private final HttpClient client;
    private final DataEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    public HttpServiceRegistry(final HttpClient client, final DataEncoding encoding, final InetSocketAddress address) {
        this.client = client;
        this.encoding = encoding;
        remoteSocketAddress = address;
    }

    @Override
    public Future<ServiceRecordResultSetData> query(final ServiceRecordQueryData query) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri("/serviceregistry/query")
                .body(encoding, query))
            .flatMap(response -> response.bodyAsClassIfSuccess(encoding, ServiceRecordResultSetData.class));
    }

    @Override
    public Future<ServiceRecordData> register(final ServiceRecordFormData form) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri("/serviceregistry/register")
                .body(encoding, form))
            .flatMap(response -> response.bodyAsClassIfSuccess(encoding, ServiceRecordData.class));
    }

    @Override
    public Future<?> unregister(final String serviceName, final String systemName, final String hostname, final int port) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.DELETE)
                .uri("/serviceregistry/unregister"))
            .flatMap(HttpClientResponse::rejectIfNotSuccess);
    }
}
