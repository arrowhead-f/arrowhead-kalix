package se.arkalix.net.http.consumer;

import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.net.http.client.HttpClient;

import java.util.Collection;
import java.util.Collections;

import static se.arkalix.descriptor.TransportDescriptor.HTTP;

public class HttpConsumerFactory implements ArConsumerFactory<HttpConsumer> {
    @Override
    public Collection<TransportDescriptor> supportedTransports() {
        return Collections.singleton(HTTP);
    }

    @Override
    public HttpConsumer create(
        final ArSystem system,
        final ServiceDescription service,
        final Collection<EncodingDescriptor> encodings) throws Exception
    {
        return new HttpConsumer(HttpClient.from(system), service, encodings);
    }
}
