package se.arkalix.io.tcp._internal;

import se.arkalix.io.IoException;
import se.arkalix.io.SocketIsClosed;
import se.arkalix.io.buffer.BufferReader;
import se.arkalix.io.tcp.TcpSocket;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Queue;

@Internal
public class NioTcpSocket implements TcpSocket {
    private final SocketChannel socketChannel;
    private final Queue<PromiseToSend> sendQueue;

    public NioTcpSocket(final SocketChannel socketChannel, final Queue<PromiseToSend> sendQueue) {
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");
        this.sendQueue = Objects.requireNonNull(sendQueue, "sendQueue");
    }

    @Override
    @ThreadSafe
    public void close() {
        try {
            socketChannel.close();
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    @ThreadSafe
    public Future<?> send(final BufferReader buffer) {
        final var promise = new PromiseToSend(buffer);
        sendQueue.add(promise);
        return promise;
    }

    @Override
    @ThreadSafe
    public <T> T option(final SocketOption<T> option) {
        try {
            return socketChannel.getOption(option);
        }
        catch (final ClosedChannelException exception) {
            throw new SocketIsClosed(exception);
        }
        catch (IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    @ThreadSafe
    public <T> TcpSocket option(final SocketOption<T> option, final T value) {
        try {
            socketChannel.setOption(option, value);
        }
        catch (final ClosedChannelException exception) {
            throw new SocketIsClosed(exception);
        }
        catch (IOException exception) {
            throw new IoException(exception);
        }
        return this;
    }

    @Override
    @ThreadSafe
    public InetSocketAddress localAddress() {
        try {
            final var localAddress = socketChannel.getLocalAddress();
            assert localAddress instanceof InetSocketAddress;
            return (InetSocketAddress) localAddress;
        }
        catch (final ClosedChannelException exception) {
            throw new SocketIsClosed(exception);
        }
        catch (IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    @ThreadSafe
    public InetSocketAddress remoteAddress() {
        try {
            final var remoteAddress = socketChannel.getRemoteAddress();
            assert remoteAddress instanceof InetSocketAddress;
            return (InetSocketAddress) remoteAddress;
        }
        catch (final ClosedChannelException exception) {
            throw new SocketIsClosed(exception);
        }
        catch (IOException exception) {
            throw new IoException(exception);
        }
    }
}
