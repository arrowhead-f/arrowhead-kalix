package se.arkalix.io.tcp._internal;

import se.arkalix.io.tcp.TcpSocket;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.time.Duration;

public class NioTcpSocket {
/*    private final AtomicReference<SelectionKey> selectionKey = new AtomicReference<>(null);
    private final SocketChannel socketChannel;

    private Flow.Subscriber<? super BufferReader> subscriber = null;

    public NioTcpSocket(
        final BufferAllocator bufferAllocator,
        final Selector selector,
        final SocketChannel socketChannel
    ) {
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");

        try {
            final var ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
            selectionKey.set(socketChannel.register(selector, ops, (Runnable) () -> {
                final var selectionKey0 = selectionKey.get();
                if (selectionKey0 == null) {
                    try {
                        subscriber.onError(new IllegalStateException("selectionKey == null"));
                    }
                    finally {
                        closeSocketChannel();
                    }
                    return;
                }
                try {
                    while (selectionKey0.isReadable()) {
                        final var buffer = bufferAllocator.allocate(8192, 8192);
                        socketChannel.read(buffer.toByteBuffers());
                    }
                    while (selectionKey0.isWritable()) {
                        final var buffer = bufferAllocator.allocate(8192, 8192); // TODO
                        socketChannel.write(buffer.toByteBuffers());
                    }
                }
                catch (final IOException exception) {
                    try {
                        subscriber.onError(new IoException(exception));
                    }
                    finally {
                        closeSocketChannel();
                    }
                }
            }));
        }
        catch (final ClosedChannelException exception) {
            throw new SocketIsClosed(exception);
        }
    }

    @Override
    public Future<?> write(final BufferReader buffer) {
        return null;
    }

    @Override
    public void close() {
        closeSocketChannel();
    }

    private void closeSocketChannel() {
        try {
            socketChannel.close();
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    public InetSocketAddress localSocketAddress() {
        try {
            return (InetSocketAddress) socketChannel.getLocalAddress();
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        try {
            return (InetSocketAddress) socketChannel.getRemoteAddress();
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    public void subscribe(final Flow.Subscriber<? super BufferReader> subscriber) {
        if (this.subscriber != null) {
            throw new IllegalStateException("subscriber already set");
        }
        this.subscriber = Objects.requireNonNull(subscriber, "subscriber");
    }*/

    public static class Connector implements TcpSocket.Connector {
        private InetSocketAddress localSocketAddress = null;
        private InetSocketAddress remoteSocketAddress = null;
        private Duration readTimeout = null;
        private Duration writeTimeout = null;
        private boolean nodelay = false;
        private long ttl = 0;

        @Override
        public TcpSocket.Connector localSocketAddress(final InetSocketAddress localSocketAddress) {
            this.localSocketAddress = localSocketAddress;
            return this;
        }

        @Override
        public TcpSocket.Connector remoteSocketAddress(final InetSocketAddress remoteSocketAddress) {
            this.remoteSocketAddress = remoteSocketAddress;
            return this;
        }

        @Override
        public TcpSocket.Connector readTimeout(final Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        @Override
        public TcpSocket.Connector writeTimeout(final Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        @Override
        public TcpSocket.Connector nodelay(final boolean nodelay) {
            this.nodelay = nodelay;
            return this;
        }

        @Override
        public TcpSocket.Connector ttl(final long ttl) {
            this.ttl = ttl;
            return this;
        }

        @Override
        public Future<TcpSocket> connect() {
            return null;
        }
    }
}
