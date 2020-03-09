package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.internal.net.NettyBootstraps;
import eu.arrowhead.kalix.internal.util.concurrent.NettyScheduler;
import eu.arrowhead.kalix.util.concurrent.Scheduler;
import io.netty.bootstrap.Bootstrap;

public class HttpClientFactory {
    private final Bootstrap bootstrap;

    public HttpClientFactory(final Scheduler scheduler) {
        if (!(scheduler instanceof NettyScheduler)) {
            throw new UnsupportedOperationException("Unsupported scheduler implementation");
        }
        this.bootstrap = NettyBootstraps.createBootstrapUsing((NettyScheduler) scheduler);
    }
}
