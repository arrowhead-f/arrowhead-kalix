package se.arkalix.io.tcp;

import se.arkalix.io.SocketOptions;
import se.arkalix.util.logging.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public interface TcpSocketOptions<S extends TcpSocketOptions<?>> extends SocketOptions<S> {
    S localAddress(InetSocketAddress localAddress);

    default S localAddress(final int port) {
        return localAddress(new InetSocketAddress(port));
    }

    default S localAddress(final InetAddress address, final int port) {
        return localAddress(new InetSocketAddress(address, port));
    }

    default S localAddress(final String hostname, final int port) {
        return localAddress(new InetSocketAddress(hostname, port));
    }

    S logger(final Logger logger);
}
