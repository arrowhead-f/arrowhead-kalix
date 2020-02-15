package eu.arrowhead.kalix.concurrent;

import java.util.function.Supplier;

/**
 * A {@link java.util.concurrent.Future} that notifies a listener when
 * completed, for any reason.
 *
 * @param <V> Type of value that can be retrieved if the operation succeeds.
 */
public interface Future<V> extends java.util.concurrent.Future<V> {
    /**
     * Sets completion listener, replacing any previous such.
     *
     * @param listener Function invoked when the {@link Future} completes.
     * @return This {@link Future}.
     */
    Future<V> onDone(final Supplier<? extends java.util.concurrent.Future<? extends V>> listener);
}
