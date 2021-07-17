package se.arkalix.io;

import java.net.SocketOption;

public interface SocketOptions<S extends SocketOptions<?>> {
    <T> T option(SocketOption<T> option);

    <T> S option(SocketOption<T> option, T value);
}
