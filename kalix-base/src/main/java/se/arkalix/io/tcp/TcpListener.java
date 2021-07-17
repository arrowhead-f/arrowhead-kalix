package se.arkalix.io.tcp;

import se.arkalix.io.SocketHandler;
import se.arkalix.io.tcp._internal.NioTcpListener;
import se.arkalix.util.concurrent.Future;

public interface TcpListener extends TcpSocketOptions<TcpListener> {
    static TcpListener create() {
        return new NioTcpListener();
    }

    TcpListener backlog(int backlog);

    Future<?> listen(SocketHandler<TcpSocket> handler);
}
