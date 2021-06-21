package se.arkalix.io.tcp;

import se.arkalix.io.buf.Buffer;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Flow;

public interface TcpSocket extends Flow.Publisher<Buffer> {
    static Connector create() {
        return null;
    }

    interface Connector {
        Connector localSocketAddress(InetSocketAddress localSocketAddress);

        Connector remoteSocketAddress(InetSocketAddress remoteSocketAddress);

        Connector readTimeout(Duration readTimeout);

        Connector writeTimeout(Duration writeTimeout);

        default Connector nodelay() {
            return nodelay(true);
        }

        Connector nodelay(boolean nodelay);

        Connector ttl(long ttl);

        Future<TcpSocket> connect();
    }
}
