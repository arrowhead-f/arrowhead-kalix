package se.arkalix.core.plugin;

import se.arkalix.ArConsumer;
import se.arkalix.core.plugin.dto.ServiceQueryDto;
import se.arkalix.core.plugin.dto.ServiceQueryResultDto;
import se.arkalix.core.plugin.dto.ServiceRegistrationDto;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead service discovery service.
 */
public interface ArServiceDiscovery extends ArConsumer {
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
     * @param serviceName Name of service of existing entry.
     * @param systemName  Name of system of existing entry.
     * @param hostname    Address/hostname of existing entry.
     * @param port        Port number of existing entry.
     * @return Future completed when unregistration is known to have succeeded
     * or failed.
     */
    Future<?> unregister(String serviceName, String systemName, String hostname, int port);
}
