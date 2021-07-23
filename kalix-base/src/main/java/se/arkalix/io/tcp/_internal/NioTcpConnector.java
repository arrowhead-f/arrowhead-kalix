package se.arkalix.io.tcp._internal;

import se.arkalix.concurrent.Future;
import se.arkalix.concurrent.SynchronizedPromise;
import se.arkalix.io.IoException;
import se.arkalix.io.SocketHandler;
import se.arkalix.io._internal.NioEventLoopGroup;
import se.arkalix.io.tcp.TcpConnector;
import se.arkalix.io.tcp.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.*;

public class NioTcpConnector extends AbstractTcpSocketOptions<TcpConnector> implements TcpConnector {
    @Override
    public Future<?> connect(final InetSocketAddress remoteAddress, final SocketHandler<TcpSocket> handler) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        if (handler == null) {
            throw new NullPointerException("handler");
        }

        final SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            for (final var option : options().entrySet()) {
                socketChannel.setOption(castOption(option.getKey()), option.getValue());
            }
            final var localAddress = localAddress().orElse(null);
            if (localAddress != null) {
                socketChannel.bind(localAddress);
            }
            socketChannel.connect(remoteAddress);
        }
        catch (final IOException exception) {
            return Future.failure(new IoException("failed to setup socket channel", exception));
        }

        final var disconnectPromise = new SynchronizedPromise<Void>();

        NioEventLoopGroup.main()
            .nextEventLoop()
            .register(socketChannel, OP_CONNECT | OP_READ | OP_WRITE, new NioTcpSocketHandler(
                socketChannel, handler, disconnectPromise, logger().orElse(null)));

        return disconnectPromise.future();
    }

    @Override
    protected TcpConnector self() {
        return this;
    }
}
