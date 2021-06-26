package se.arkalix.io.evt;

import se.arkalix.util.concurrent.Future;

/**
 * Low-level input/output scheduler.
 */
public interface EventLoop {
    static EventLoop main() {
        return null;
    }

    //Future<TcpSocket> connect(TcpSocket.Options options);

    //Future<TcpListener> listen(TcpListener.Options options);

    //Future<File> open(File.Options options);

    //<V> Task<V> schedule(Task.Options<V> task);

    Future<?> shutdown();
}
