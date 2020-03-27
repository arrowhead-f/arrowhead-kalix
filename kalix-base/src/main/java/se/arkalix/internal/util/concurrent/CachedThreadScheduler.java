package se.arkalix.internal.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadScheduler {
    private CachedThreadScheduler() {}

    public static final ExecutorService executorService = Executors.newCachedThreadPool();
}
