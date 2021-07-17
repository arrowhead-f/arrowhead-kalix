package se.arkalix.io.tcp._internal;

import se.arkalix.io.IoException;
import se.arkalix.io.SocketHandler;
import se.arkalix.io.SocketReceiver;
import se.arkalix.io._internal.NioEventLoop;
import se.arkalix.io._internal.NioSocketReader;
import se.arkalix.io.tcp.TcpSocket;
import se.arkalix.util.concurrent.Promise;
import se.arkalix.util.logging.Event;
import se.arkalix.util.logging.Logger;
import se.arkalix.util.logging.Loggers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioTcpSocketHandler implements NioEventLoop.Handler {
    private final Promise<?> disconnectPromise;
    private final Logger logger;
    private final ConcurrentLinkedQueue<PromiseToSend> sendQueue;
    private final NioSocketReader socketReader;
    private final NioTcpSocket socket;
    private final SocketChannel socketChannel;
    private final SocketHandler<TcpSocket> handler;

    private SocketReceiver socketReceiver = null;

    public NioTcpSocketHandler(
        final SocketChannel socketChannel,
        final SocketHandler<TcpSocket> handler,
        final Promise<?> disconnectPromise,
        final Logger logger
    )
    {
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.disconnectPromise = Objects.requireNonNull(disconnectPromise, "disconnectPromise");
        this.logger = Objects.requireNonNullElseGet(logger, Loggers::text);

        sendQueue = new ConcurrentLinkedQueue<>();
        socketReader = new NioSocketReader(socketChannel);
        socket = new NioTcpSocket(socketChannel, sendQueue);
    }

    @Override
    public void handle(final SelectionKey key) {
        try {
            if (!key.isValid()) {
                if (socketChannel.isOpen()) {
                    disconnect(null);
                }
                return;
            }
            if (key.isConnectable()) {
                final var isConnected = socketChannel.finishConnect();
                assert isConnected;

                socketReceiver = handler.handle(socket);
                if (socketReceiver == null) {
                    socketChannel.shutdownInput();
                }
            }
            if (key.isReadable()) {
                socketReceiver.receive(socketReader);
            }
            if (key.isWritable()) {
                for (PromiseToSend next; (next = sendQueue.peek()) != null; ) {
                    if (next.send(socketChannel) > 0) {
                        sendQueue.poll();
                    }
                    else {
                        disconnect(null);
                    }
                }
            }

        }
        catch (Throwable throwable) {
            if (throwable instanceof IOException) {
                throwable = new IoException(throwable);
            }
            disconnect(throwable);
        }
    }

    private void disconnect(Throwable throwable) {
        try {
            socketChannel.close();
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
                disconnectPromise.forfeit(throwable);
            }
            else {
                disconnectPromise.fulfill();
            }
        }
        catch (final Throwable throwable0) {
            if (throwable != null) {
                throwable0.addSuppressed(throwable);
            }
            logger.log(new Event("CompletePromiseWhileDisconnecting")
                .withContext(NioTcpSocketHandler.class)
                .withException(throwable0)
                .withMessage("failed to complete TCP disconnect promise"));
        }
    }
}
