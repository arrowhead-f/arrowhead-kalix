package se.arkalix.io;

@FunctionalInterface
public interface SocketReceiver {
    void receive(SocketReader reader) throws Throwable;
}
