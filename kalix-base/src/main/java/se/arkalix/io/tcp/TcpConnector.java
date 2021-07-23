package se.arkalix.io.tcp;

import se.arkalix.concurrent.Future;
import se.arkalix.io.SocketHandler;
import se.arkalix.io.tcp._internal.NioTcpConnector;

import java.net.InetSocketAddress;

public interface TcpConnector extends TcpSocketOptions<TcpConnector> {
    static TcpConnector create() {
        return new NioTcpConnector();
    }

    Future<?> connect(InetSocketAddress remoteAddress, SocketHandler<TcpSocket> handler);
}
