package se.arkalix.io.tcp._internal;

import se.arkalix.io.IoException;
import se.arkalix.io.tcp.TcpAcceptor;
import se.arkalix.io.tcp.TcpListener;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.function.ThrowingConsumer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NioTcpListener implements TcpListener {
    private final Map<SocketOption<Object>, Object> socketOptions = new HashMap<>();

    private InetSocketAddress localInetSocketAddress = null;
    private int backlog = 0;

    @Override
    public int backlog() {
        return backlog;
    }

    @Override
    public TcpListener backlog(final int backlog) {
        this.backlog = backlog;
        return this;
    }

    @Override
    public InetSocketAddress localInetSocketAddress() {
        return localInetSocketAddress != null
            ? localInetSocketAddress
            : new InetSocketAddress(0);
    }

    @Override
    public TcpListener localInetSocketAddress(final InetSocketAddress localInetSocketAddress) {
        this.localInetSocketAddress = localInetSocketAddress;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> option(final SocketOption<T> name) {
        return Optional.ofNullable((T) socketOptions.get(name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TcpListener option(final SocketOption<T> name, final T value) {
        socketOptions.put((SocketOption<Object>) name, value);
        return this;
    }

    @Override
    public Future<?> listen(final ThrowingConsumer<TcpAcceptor> consumer) {
        try {
            final var serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(localInetSocketAddress(), backlog);
            for (final var entry : socketOptions.entrySet()) {
                try {
                    serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
                catch (final UnsupportedOperationException exception) {
                    throw new IoException(exception); // TODO.
                }
            }
            // TODO.
            return null;
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }
}
