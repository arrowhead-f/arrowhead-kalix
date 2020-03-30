package se.arkalix.internal.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Scheduler;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Internal
abstract class AbstractScheduler implements Scheduler {
    protected abstract ScheduledExecutorService executor();

    @Override
    public void execute(final Runnable command) {
        executor().execute(command);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        try {
            final var adapter = new CompletableFuture<>();
            final var future = executor().submit(() -> {
                Result<Object> result;
                try {
                    task.run();
                    result = Result.success(null);
                }
                catch (final Throwable throwable) {
                    result = Result.failure(throwable);
                }
                adapter.complete(result);
            });
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <V> Future<V> submit(final Callable<V> task) {
        try {
            final var adapter = new CompletableFuture<V>();
            final var future = executor().submit(() -> {
                Result<V> result;
                try {
                    result = Result.success(task.call());
                }
                catch (final Throwable throwable) {
                    result = Result.failure(throwable);
                }
                adapter.complete(result);
            });
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <V> Future<V> submit(final Runnable task, final V result) {
        try {
            final var adapter = new CompletableFuture<V>();
            final var future = executor().submit(() -> {
                Result<V> result0;
                try {
                    task.run();
                    result0 = Result.success(result);
                }
                catch (final Throwable throwable) {
                    result0 = Result.failure(throwable);
                }
                adapter.complete(result0);
            });
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<?> schedule(final Duration delay, final Runnable command) {
        try {
            final var adapter = new CompletableFuture<>();
            final var future = executor().schedule(() -> {
                Result<Object> result0;
                try {
                    command.run();
                    result0 = Result.done();
                }
                catch (final Throwable throwable) {
                    result0 = Result.failure(throwable);
                }
                adapter.complete(result0);
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <V> Future<V> schedule(final Duration delay, final Callable<V> callable) {
        try {
            final var adapter = new CompletableFuture<V>();
            final var future = executor().schedule(() -> {
                Result<V> result0;
                try {
                    result0 = Result.success(callable.call());
                }
                catch (final Throwable throwable) {
                    result0 = Result.failure(throwable);
                }
                adapter.complete(result0);
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<?> scheduleAtFixedRate(final Duration initialDelay, final Duration rate, final Runnable command) {
        try {
            final var adapter = new CompletableFuture<>();
            final var future = executor().scheduleAtFixedRate(() -> {
                try {
                    command.run();
                }
                catch (final Throwable throwable) {
                    adapter.complete(Result.failure(throwable));
                    adapter.cancel(false);
                }
            }, initialDelay.toMillis(), rate.toMillis(), TimeUnit.MILLISECONDS);
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<?> scheduleWithFixedDelay(final Duration initalDelay, final Duration delay, final Runnable command) {
        try {
            final var adapter = new CompletableFuture<>();
            final var future = executor().scheduleWithFixedDelay(() -> {
                try {
                    command.run();
                }
                catch (final Throwable throwable) {
                    adapter.complete(Result.failure(throwable));
                    adapter.cancel(false);
                }
            }, initalDelay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
            adapter.setCancelFunction(future::cancel);
            return adapter;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    public abstract void shutdown();
}
