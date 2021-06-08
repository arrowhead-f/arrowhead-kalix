package se.arkalix.util.concurrent._internal;

import se.arkalix.util.concurrent.Future;

public class FutureConsumptionWithExtraCancelTarget<V> extends FutureConsumption<V> {
    private Future<?> extraCancelTarget = null;

    public FutureConsumptionWithExtraCancelTarget(final Future<?> parent) {
        super(parent);
    }

    public void extraCancelTarget(final Future<?> extraCancelTarget) {
        this.extraCancelTarget = extraCancelTarget;
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        try {
            super.cancel(mayInterruptIfRunning);
        }
        finally {
            if (extraCancelTarget != null) {
                extraCancelTarget.cancel(mayInterruptIfRunning);
            }
        }
    }
}
