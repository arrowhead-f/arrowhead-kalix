package se.arkalix.io.tcp._internal;

import se.arkalix.io.tcp.TcpSocketOptions;
import se.arkalix.util.logging.Logger;

import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractTcpSocketOptions<S extends TcpSocketOptions<?>> implements TcpSocketOptions<S> {
    private final HashMap<SocketOption<?>, Object> options = new HashMap<>();

    private InetSocketAddress localSocketAddress = null;
    private Logger logger = null;

    protected abstract S self();

    public Optional<Logger> logger() {
        return Optional.ofNullable(logger);
    }

    @Override
    public S logger(final Logger logger) {
        this.logger = logger;
        return self();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T option(final SocketOption<T> option) {
        return (T) options.get(option);
    }

    @Override
    public <T> S option(final SocketOption<T> option, final T value) {
        options.put(option, value);
        return self();
    }

    @SuppressWarnings("unchecked")
    protected static SocketOption<Object> castOption(final SocketOption<?> option) {
        return (SocketOption<Object>) option;
    }

    public Map<SocketOption<?>, Object> options() {
        return Collections.unmodifiableMap(options);
    }

    public Optional<InetSocketAddress> localAddress() {
        return Optional.ofNullable(localSocketAddress);
    }

    @Override
    public S localAddress(final InetSocketAddress localAddress) {
        this.localSocketAddress = localAddress;
        return self();
    }
}
