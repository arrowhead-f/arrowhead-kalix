package se.arkalix.util.concurrent._internal;

import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Promise;

@Internal
public class ChainedPromise<V> extends Promise<V> {
    private final Future<?> parent;

    public ChainedPromise(final Future<?> parent) {
        this.parent = parent;
    }

    @Override
    public boolean cancel() {
        if (super.cancel()) {
            return parent.cancel();
        }
        return false;
    }
}
