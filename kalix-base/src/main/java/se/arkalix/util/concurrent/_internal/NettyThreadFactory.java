package se.arkalix.util.concurrent._internal;

import se.arkalix.util.annotation.Internal;

import java.util.concurrent.ThreadFactory;

@Internal
public class NettyThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(final Runnable runnable) {
        return new NettyThread(runnable);
    }
}
