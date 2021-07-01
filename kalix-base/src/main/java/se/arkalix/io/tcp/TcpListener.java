package se.arkalix.io.tcp;

import se.arkalix.io.Listener;
import se.arkalix.io.tcp._internal.NioTcpListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.util.Optional;
import java.util.Set;

public interface TcpListener extends Listener<TcpAcceptor> {
    static TcpListener create() {
        return new NioTcpListener();
    }

    int backlog();

    TcpListener backlog(int backlog);

    InetSocketAddress localInetSocketAddress();

    TcpListener localInetSocketAddress(final InetSocketAddress localInetSocketAddress);

    <T> Optional<T> option(SocketOption<T> name);

    <T> TcpListener option(SocketOption<T> name, T value);
}
