package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.util.annotation.Internal;

@Internal
@FunctionalInterface
public interface HttpResponseReceiver {
    void receive(final HttpClientResponse response);
}

