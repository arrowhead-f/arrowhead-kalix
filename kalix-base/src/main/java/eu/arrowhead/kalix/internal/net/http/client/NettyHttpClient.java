package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class NettyHttpClient implements HttpClient {
    private Channel channel = null;

    @Override
    public InetSocketAddress remoteSocketAddress() {
        return null;
    }

    /**
     * @return Local network interface bound to this client.
     */
    @Override
    public InetSocketAddress localSocketAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClient}.
     *
     * @param request HTTP request to send.
     * @return Future of {@code HttpClientResponse}.
     */
    @Override
    public Future<HttpClientResponse> send(final HttpClientRequest request) {
        return Future.failure(new UnsupportedOperationException()); // TODO.
    }

    /**
     * Attempts to close the client, destroying any connection with its remote
     * host.
     *
     * @return Future completed when closing is done.
     */
    @Override
    public Future<?> close() {
        return adapt(channel.close());
    }

    public void receive(final HttpClientResponse response) {

    }

    public void setChannel(final Channel channel) {
        this.channel = channel;
    }
}
