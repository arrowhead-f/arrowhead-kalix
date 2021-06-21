package se.arkalix.io.tcp;

import java.util.concurrent.Flow;

public interface TcpListener extends Flow.Publisher<TcpSocket> {
}
