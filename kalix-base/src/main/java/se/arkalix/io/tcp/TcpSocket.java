package se.arkalix.io.tcp;

import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.evt.EventLoop;
import se.arkalix.io.tcp._internal.NioTcpSocket;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Scheduler;
import se.arkalix.util.logging.Logger;

import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.time.Duration;

public interface TcpSocket extends Future<TcpSocket.DisconnectCause> {
    static Connector create() {
        return new NioTcpSocket.Connector();
    }

    Future<?> send(BufferReader buffer);

    InetSocketAddress localSocketAddress();

    InetSocketAddress remoteSocketAddress();

    Duration readTimeout();

    void readTimeout(Duration readTimeout);

    Duration writeTimeout();

    void writeTimeout(Duration writeTimeout);

    <T> T option(SocketOption<T> option);

    <T> void option(SocketOption<T> option, T value);

    void close();

    interface Connector {
        Connector receiver(Receiver receiver);

        Connector logger(Logger logger);

        Connector eventLoop(EventLoop eventLoop);

        Connector localSocketAddress(InetSocketAddress localSocketAddress);

        Connector remoteSocketAddress(InetSocketAddress remoteSocketAddress);

        Connector readTimeout(Duration readTimeout);

        Connector writeTimeout(Duration writeTimeout);

        <T> Connector option(SocketOption<T> option, T value);

        Future<TcpSocket> connect();
    }

    enum DisconnectCause {
        CLOSED_BY_PEER,
        CLOSED_BY_SELF,
    }

    @FunctionalInterface
    interface Receiver {
        void onReceive(TcpSocket socket, BufferReader buffer);
    }
}
