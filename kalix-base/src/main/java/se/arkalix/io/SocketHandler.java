package se.arkalix.io;

@FunctionalInterface
public interface SocketHandler<T extends Socket<?>> {
    SocketReceiver handle(T socket) throws Throwable;
}
