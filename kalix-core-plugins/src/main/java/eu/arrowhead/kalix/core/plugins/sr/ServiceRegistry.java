package eu.arrowhead.kalix.core.plugins.sr;

import eu.arrowhead.kalix.core.plugins.sr.dto.*;
import eu.arrowhead.kalix.util.concurrent.Future;

public interface ServiceRegistry {
    Future<ServiceRecordResultSetData> query(ServiceRecordQueryData query);

    Future<ServiceRecordData> register(ServiceRecordFormData form);

    Future<?> unregister(final String serviceName, final String systemName, String hostname, int port);
}
