package se.arkalix.io;

public interface Acceptor<S extends Sender<?>> {
    S accept(Receiver<S> receiver);

    void reject();
}
