package eu.arrowhead.kalix.core.plugins.srv;

import eu.arrowhead.kalix.core.plugins.srv.dto.ServiceRegistrationData;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Represents an Arrowhead service registry.
 */
public interface AhfServiceRegistry {
    /**
     * Makes an attempt to register a service with the registry.
     *
     * @param registration Description of service.
     * @return Future completed when the registration attempt is known to have
     * succeeded or failed.
     */
    Future<?> register(ServiceRegistrationData registration);

    /**
     * Makes an attempt to unregister a service that is currently registered
     * with the registry.
     *
     * @param serviceName         Name of service in existing entry.
     * @param systemName          Name of system in existing entry.
     * @param systemSocketAddress Hostname and port in existing entry.
     * @return Future completed when unregistration is known to have succeeded
     * or failed.
     */
    Future<?> unregister(String serviceName, String systemName, InetSocketAddress systemSocketAddress);
}
