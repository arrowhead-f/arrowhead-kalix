package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.ServiceQueryDto;
import se.arkalix.core.plugin.dto.ServiceQueryResultDto;
import se.arkalix.core.plugin.dto.ServiceRegistrationDto;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Represents an Arrowhead service registry.
 */
public interface ArServiceRegistry {
    /**
     * Queries registry for certain service definitions.
     *
     * @param query Description of what service definitions are desired.
     * @return Future completed with the results of the query, if no errors
     * occurred.
     */
    Future<ServiceQueryResultDto> query(ServiceQueryDto query);

    /**
     * Registers a service with the registry.
     *
     * @param registration Description of service.
     * @return Future completed when the registration attempt is known to have
     * succeeded or failed.
     */
    Future<?> register(ServiceRegistrationDto registration);

    /**
     * Unregister a service that is currently registered with the registry.
     *
     * @param serviceName         Name of service in existing entry.
     * @param systemName          Name of system in existing entry.
     * @param systemSocketAddress Hostname and port in existing entry.
     * @return Future completed when unregistration is known to have succeeded
     * or failed.
     */
    Future<?> unregister(String serviceName, String systemName, InetSocketAddress systemSocketAddress);
}
