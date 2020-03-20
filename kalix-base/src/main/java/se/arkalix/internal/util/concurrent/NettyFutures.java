package se.arkalix.internal.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.function.Consumer;

@Internal
public class NettyFutures {
    private NettyFutures() {}

    public static Future<Channel> adapt(final io.netty.channel.ChannelFuture channelFuture) {
        return new ChannelFutureAdapter(channelFuture);
    }

    public static <V> Future<V> adapt(final io.netty.util.concurrent.Future<V> future) {
        return new FutureAdapter<>(future);
    }

    private static class ChannelFutureAdapter implements Future<Channel> {
        private final io.netty.channel.ChannelFuture future;
        private GenericFutureListener<io.netty.channel.ChannelFuture> listener = null;

        private ChannelFutureAdapter(final ChannelFuture future) {
            this.future = future;
        }

        @Override
        public void onResult(final Consumer<Result<Channel>> consumer) {
            if (listener != null) {
                future.removeListener(listener);
            }
            future.addListener(listener = future -> consumer.accept(future.isSuccess()
                ? Result.success(future.channel())
                : Result.failure(future.cause())));
        }

        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            if (future.isCancellable()) {
                future.cancel(mayInterruptIfRunning);
            }
            if (listener != null) {
                future.removeListener(listener);
            }
        }
    }

    private static class FutureAdapter<V> implements Future<V> {
        private final io.netty.util.concurrent.Future<V> future;
        private GenericFutureListener<io.netty.util.concurrent.Future<V>> listener = null;

        private FutureAdapter(final io.netty.util.concurrent.Future<V> future) {
            this.future = future;
        }

        @Override
        public void onResult(final Consumer<Result<V>> consumer) {
            if (listener != null) {
                future.removeListener(listener);
            }
            future.addListener(listener = future -> consumer.accept(future.isSuccess()
                ? Result.success(future.get())
                : Result.failure(future.cause())));
        }

        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            if (future.isCancellable()) {
                future.cancel(mayInterruptIfRunning);
            }
            if (listener != null) {
                future.removeListener(listener);
            }
        }
    }
}
