package se.arkalix.concurrent._internal;

import se.arkalix.util.annotation.Internal;
import se.arkalix.concurrent.Future;
import se.arkalix.concurrent.Promise;

@Internal
public class ChainedPromise<V> extends Promise<V> {
    private final Future<?> parent;

    public ChainedPromise(final Future<?> parent) {
        this.parent = parent;
    }

    @Override
    protected void onCancelled() {
        parent.cancel();
    }
}
