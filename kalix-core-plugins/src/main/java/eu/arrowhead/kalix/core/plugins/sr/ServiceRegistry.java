package eu.arrowhead.kalix.core.plugins.sr;

import eu.arrowhead.kalix.core.plugins.sr.dto.*;
import eu.arrowhead.kalix.util.concurrent.Future;

public interface ServiceRegistry {
    Future<ServiceRecordResultSet> query(ServiceRecordQuery query);

    Future<ServiceRecord> register(ServiceRecordForm form);

    Future<?> unregister(final String serviceName, final String systemName, String hostname, int port);
}
