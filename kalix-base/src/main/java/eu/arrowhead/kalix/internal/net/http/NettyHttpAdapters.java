package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Various adapters useful for converting HTTP types to/from their Kalix/Netty
 * representations.
 */
@Internal
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
    public static HttpResponseStatus adapt(final HttpStatus status) {
        return HttpResponseStatus.valueOf(status.code());
    }

    /**
     * @param status Netty HTTP status.
     * @return Kalix HTTP status.
     */
    public static HttpStatus adapt(final HttpResponseStatus status) {
        return HttpStatus.valueOf(status.code());
    }
}
