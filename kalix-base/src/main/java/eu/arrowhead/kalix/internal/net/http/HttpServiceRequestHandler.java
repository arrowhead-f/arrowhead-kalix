package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;
import eu.arrowhead.kalix.util.concurrent.Future;

@FunctionalInterface
public interface HttpServiceRequestHandler {
    Future<HttpServiceResponse> handle(final HttpServiceRequest request);
}
