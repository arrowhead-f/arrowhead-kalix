package se.arkalix;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;

import java.util.Collection;

public interface ArConsumerFactory<C extends ArConsumer> {
    default Collection<EncodingDescriptor> supportedEncodings() {
        return EncodingDescriptor.dtoEncodings();
    }

    Collection<TransportDescriptor> supportedTransports();

    C create(ArSystem system, ServiceDescription service, Collection<EncodingDescriptor> encodings) throws Exception;
}
