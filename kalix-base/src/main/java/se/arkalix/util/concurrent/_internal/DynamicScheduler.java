package se.arkalix.util.concurrent._internal;

import se.arkalix.util.annotation.Internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Internal
public class DynamicScheduler extends AbstractScheduler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);

    @Override
    protected ScheduledExecutorService executor() {
        return executor;
    }

}
