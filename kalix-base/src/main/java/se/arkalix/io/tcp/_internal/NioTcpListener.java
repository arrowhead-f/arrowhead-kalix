package se.arkalix.io.tcp._internal;

import se.arkalix.concurrent.Future;
import se.arkalix.concurrent.SynchronizedPromise;
import se.arkalix.io.IoException;
import se.arkalix.io.SocketHandler;
import se.arkalix.io._internal.NioEventLoop;
import se.arkalix.io._internal.NioEventLoopGroup;
import se.arkalix.io.tcp.TcpListener;
import se.arkalix.io.tcp.TcpSocket;
import se.arkalix.util.logging.Event;
import se.arkalix.util.logging.Loggers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import static java.nio.channels.SelectionKey.*;

public class NioTcpListener extends AbstractTcpSocketOptions<TcpListener> implements TcpListener {
    private int backlog = 0;

    @Override
    public TcpListener backlog(final int backlog) {
        this.backlog = Math.max(backlog, 0);
        return null;
    }

    @Override
    public Future<?> listen(final SocketHandler<TcpSocket> handler) {
        if (handler == null) {
            throw new NullPointerException("handler");
        }

        final ServerSocketChannel serverSocketChannel;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            for (final var option : options().entrySet()) {
                serverSocketChannel.setOption(castOption(option.getKey()), option.getValue());
            }
            serverSocketChannel.bind(localAddress().orElse(null), backlog);
        }
        catch (final IOException exception) {
            return Future.failure(new IoException("failed to setup socket channel", exception));
        }

        final var promiseToStopListening = new SynchronizedPromise<Void>();
        final var eventLoopGroup = NioEventLoopGroup.main();

        eventLoopGroup.nextEventLoop()
            .register(serverSocketChannel, OP_ACCEPT, new NioEventLoop.Handler() {
                @Override
                public void handle(final SelectionKey key) {
                    try {
                        if (!key.isValid()) {
                            if (serverSocketChannel.isOpen()) {
                                stopListening(null);
                            }
                            return;
                        }
                        if (key.isAcceptable()) {
                            final var socketChannel = serverSocketChannel.accept();
                            assert socketChannel != null;

                            socketChannel.configureBlocking(false);
                            eventLoopGroup.nextEventLoop()
                                .register(socketChannel, OP_CONNECT | OP_READ | OP_WRITE, new NioTcpSocketHandler(
                                    socketChannel, handler, null, logger().orElse(null)));
                        }
                    }
                    catch (Throwable throwable) {
                        if (throwable instanceof IOException) {
                            throwable = new IoException(throwable);
                        }
                        stopListening(throwable);
                    }
                }

                private void stopListening(Throwable throwable) {
                    try {
                        serverSocketChannel.close();
                    }
                    catch (final Throwable throwable0) {
                        if (throwable == null) {
                            throwable = throwable0;
                        }
                        else {
                            throwable.addSuppressed(throwable0);
                        }
                    }
                    try {
                        if (throwable != null) {
                            promiseToStopListening.fail(throwable);
                        }
                        else {
                            promiseToStopListening.fulfill(null);
                        }
                    }
                    catch (final Throwable throwable0) {
                        if (throwable != null) {
                            throwable0.addSuppressed(throwable);
                        }
                        logger()
                            .orElse(Loggers.text())
                            .log(new Event("CompletePromiseWhileDisconnecting")
                                .withContext(NioTcpListener.class)
                                .withException(throwable0)
                                .withMessage("failed to complete TCP disconnect promise"));
                    }
                }
            });

        return promiseToStopListening.future();
    }

    @Override
    protected TcpListener self() {
        return this;
    }
}
