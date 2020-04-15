package se.arkalix.core.plugin;

import java.net.InetSocketAddress;

/**
 * Use {@link HttpJsonCloudPlugin} instead.
 */
@Deprecated(since = "0.2.1", forRemoval = true)
public class HttpJsonCoreIntegrator {
    /**
     * Use {@link HttpJsonCloudPlugin#viaServiceRegistryAt(InetSocketAddress)}
     * instead.
     */
    @Deprecated(since = "0.2.1", forRemoval = true)
    public static HttpJsonCloudPlugin viaServiceRegistryAt(final InetSocketAddress socketAddress) {
        return new HttpJsonCloudPlugin.Builder()
            .serviceRegistrySocketAddress(socketAddress)
            .build();
    }

    /**
     * Use {@link HttpJsonCloudPlugin.Builder} instead.
     */
    @Deprecated(since = "0.2.1", forRemoval = true)
    public static class Builder extends HttpJsonCloudPlugin.Builder {}
}
