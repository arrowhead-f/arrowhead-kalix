package eu.arrowhead.kalix.descriptor;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

public class SystemDescriptor {
    private final String name;
    private final InetSocketAddress socketAddress;
    private final X509Certificate certificate;

    public SystemDescriptor(final String name, final InetSocketAddress address, final X509Certificate key) {
        this.name = name;
        socketAddress = address;
        certificate = key;
    }

    public String name() {
        return name;
    }

    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    public X509Certificate certificate() {
        return certificate;
    }
}
