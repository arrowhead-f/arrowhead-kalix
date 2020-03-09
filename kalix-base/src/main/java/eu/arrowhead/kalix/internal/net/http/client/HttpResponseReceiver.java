package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.util.annotation.Internal;

@Internal
public interface HttpResponseReceiver {
    EncodingDescriptor[] encodings();

    void receive(final HttpClientResponse response);

    void fail(final Throwable throwable);
}

