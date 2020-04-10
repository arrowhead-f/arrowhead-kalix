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

/**
 * Class used for creating {@link HttpConsumer} instances.
 * <p>
 * This class is primarily useful as input to the {@link
 * se.arkalix.query.ServiceQuery#using(ArConsumerFactory) using()} method of
 * the {@link se.arkalix.query.ServiceQuery ServiceQuery} class, an instance of
 * which is returned by the {@link ArSystem#consume()} method. See the {@link
 * se.arkalix.net.http.consumer package documentation} for more details about
 * how this class can be used.
 * <p>
 * Use the {@link HttpConsumer#factory()} method to get an instance of this
 * class.
 */
public class HttpConsumerFactory implements ArConsumerFactory<HttpConsumer> {
    @Override
    public Collection<TransportDescriptor> serviceTransports() {
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
