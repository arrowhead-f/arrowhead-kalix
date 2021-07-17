package se.arkalix.io.tcp;

import se.arkalix.io.Socket;

import java.net.InetSocketAddress;

public interface TcpSocket extends Socket<TcpSocket> {
    InetSocketAddress localAddress();

    InetSocketAddress remoteAddress();
}
