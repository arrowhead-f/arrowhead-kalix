package se.arkalix.io;

import se.arkalix.util.concurrent.Future;
import se.arkalix.util.function.ThrowingConsumer;

public interface Listener<A extends Acceptor<?>> {
    Future<?> listen(ThrowingConsumer<A> consumer);
}
