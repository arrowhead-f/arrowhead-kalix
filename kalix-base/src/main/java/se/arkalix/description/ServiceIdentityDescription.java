package se.arkalix.description;

/**
 * Describes a service with a known {@link #provider() provider identity}, if
 * the described system is running in {@link se.arkalix.security secure mode}.
 */
public interface ServiceIdentityDescription extends ServiceDescription {
    /**
     * @return Description of system providing this service.
     */
    @Override
    SystemIdentityDescription provider();
}
