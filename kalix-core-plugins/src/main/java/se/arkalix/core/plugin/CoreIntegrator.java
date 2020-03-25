package se.arkalix.core.plugin;

import se.arkalix.plugin.Plugin;

import java.net.InetSocketAddress;
import java.security.PublicKey;

public class CoreIntegrator implements Plugin {

    public static class Builder {
        private InetSocketAddress serviceRegistrySocketAddress;
        private PublicKey authorizationSystemPublicKey;
        
    }
}
