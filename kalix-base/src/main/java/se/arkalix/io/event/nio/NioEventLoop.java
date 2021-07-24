package se.arkalix.io.event.nio;

import se.arkalix.io.event.EventLoop;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class NioEventLoop implements EventLoop {
    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public <T> SchedulerFuture<T> submit(final Callable<T> task) {
        return null;
    }

    @Override
    public <T> SchedulerFuture<T> submit(final Runnable task, final T result) {
        return null;
    }

    @Override
    public SchedulerFuture<?> submit(final Runnable task) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public SchedulerFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return null;
    }

    @Override
    public <V> SchedulerFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        return null;
    }

    @Override
    public SchedulerFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        return null;
    }

    @Override
    public SchedulerFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        return null;
    }

    @Override
    public void execute(final Runnable command) {

    }
}
