package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;

/**
 * Various adapters useful for converting HTTP types to/from their Kalix/Netty
 * representations.
 */
public class NettyHttpAdapters {
    private NettyHttpAdapters() {}

    /**
     * @param version Netty HTTP version.
     * @return Kalix HTTP version.
     */
    public static HttpVersion adapt(final io.netty.handler.codec.http.HttpVersion version) {
        return HttpVersion.getOrCreate(version.majorVersion(), version.minorVersion());
    }

    /**
     * @param method Netty HTTP method.
     * @return Kalix HTTP method.
     */
    public static HttpMethod adapt(final io.netty.handler.codec.http.HttpMethod method) {
        return HttpMethod.valueOf(method.name());
    }

    /**
     * @param status Kalix HTTP status.
     * @return Netty HTTP status.
     */
    public static io.netty.handler.codec.http.HttpResponseStatus adapt(final HttpStatus status) {
        return io.netty.handler.codec.http.HttpResponseStatus.valueOf(status.code());
    }
}
