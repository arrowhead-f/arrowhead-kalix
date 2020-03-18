package eu.arrowhead.kalix.core.plugins.srv;

import eu.arrowhead.kalix.core.plugins.srv.dto.ServiceRecordFormData;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;

public interface AhfServiceRegistry {
    Future<?> register(ServiceRecordFormData form);

    Future<?> unregister(String serviceName, String systemName, InetSocketAddress systemSocketAddress);
}
