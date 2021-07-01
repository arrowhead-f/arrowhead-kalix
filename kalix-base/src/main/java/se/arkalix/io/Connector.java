package se.arkalix.io;

import se.arkalix.util.concurrent.Future;

public interface Connector<S extends Sender<?>> {
    Future<S> connect(Receiver<S> receiver);
}
