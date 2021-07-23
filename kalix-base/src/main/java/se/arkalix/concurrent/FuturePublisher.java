package se.arkalix.concurrent;

import se.arkalix.util._internal.Throwables;
import se.arkalix.util.annotation.ThreadSafe;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FuturePublisher<T> {
    private Set<Promise<T>> subscribers = new HashSet<>();
    private Future<T> future = null;

    FuturePublisher(final Future<T> future) {
        Objects.requireNonNull(future)
            .onCompletion(result -> {
                Throwable exception = null;

                final var iterator = subscribers.iterator();

                for (Promise<T> next; (next = iterator.next()) != null; ) {
                    try {
                        next.complete(result);
                    }
                    catch (final Throwable throwable) {
                        Throwables.throwSilentlyIfFatal(throwable);
                        if (exception == null) {
                            exception = throwable;
                        }
                        else {
                            exception.addSuppressed(throwable);
                        }
                    }

                    iterator.remove();
                }

                subscribers = null;

                if (exception != null) {
                    Throwables.throwSilently(exception);
                }

                this.future = Future.of(result);
            });
    }

    @ThreadSafe
    public Future<T> subscribe() {
        if (future != null) {
            return future;
        }

        final var promise = new SynchronizedPromise<T>();

        subscribers.add(promise);

        return promise.future();
    }
}
