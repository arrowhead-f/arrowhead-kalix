package se.arkalix.net.http._internal;

import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.util.annotation.Internal;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Various adapters useful for converting HTTP types to/from their Kalix/Netty
 * representations.
 */
@Internal
public class NettyHttpConverters {
    private NettyHttpConverters() {}

    /**
     * @param version Netty HTTP version.
     * @return Kalix HTTP version.
     */
    public static HttpVersion convert(final io.netty.handler.codec.http.HttpVersion version) {
        if (version == io.netty.handler.codec.http.HttpVersion.HTTP_1_1) {
            return HttpVersion.HTTP_11;
        }
        if (version == io.netty.handler.codec.http.HttpVersion.HTTP_1_0) {
            return HttpVersion.HTTP_10;
        }
        return HttpVersion.getOrCreate(version.majorVersion(), version.minorVersion());
    }

    /**
     * @param version Kalix HTTP version.
     * @return Netty HTTP version.
     */
    public static io.netty.handler.codec.http.HttpVersion convert(final HttpVersion version) {
        if (version == HttpVersion.HTTP_11) {
            return io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
        }
        if (version == HttpVersion.HTTP_10) {
            return io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
        }
        return io.netty.handler.codec.http.HttpVersion.valueOf(version.toString());
    }

    /**
     * @param method Netty HTTP method.
     * @return Kalix HTTP method.
     */
    public static HttpMethod convert(final io.netty.handler.codec.http.HttpMethod method) {
        if (method == io.netty.handler.codec.http.HttpMethod.GET) {
            return HttpMethod.GET;
        }
        if (method == io.netty.handler.codec.http.HttpMethod.POST) {
            return HttpMethod.POST;
        }
        if (method == io.netty.handler.codec.http.HttpMethod.PUT) {
            return HttpMethod.PUT;
        }
        if (method == io.netty.handler.codec.http.HttpMethod.DELETE) {
            return HttpMethod.DELETE;
        }
        return HttpMethod.valueOf(method.name());
    }

    /**
     * @param method Netty HTTP method.
     * @return Kalix HTTP method.
     */
    public static io.netty.handler.codec.http.HttpMethod convert(final HttpMethod method) {
        if (method == HttpMethod.GET) {
            return io.netty.handler.codec.http.HttpMethod.GET;
        }
        if (method == HttpMethod.POST) {
            return io.netty.handler.codec.http.HttpMethod.POST;
        }
        if (method == HttpMethod.PUT) {
            return io.netty.handler.codec.http.HttpMethod.PUT;
        }
        if (method == HttpMethod.DELETE) {
            return io.netty.handler.codec.http.HttpMethod.DELETE;
        }
        return io.netty.handler.codec.http.HttpMethod.valueOf(method.name());
    }

    /**
     * @param status Kalix HTTP status.
     * @return Netty HTTP status.
     */
    public static HttpResponseStatus convert(final HttpStatus status) {
        return HttpResponseStatus.valueOf(status.code());
    }

    /**
     * @param status Netty HTTP status.
     * @return Kalix HTTP status.
     */
    public static HttpStatus convert(final HttpResponseStatus status) {
        return HttpStatus.valueOf(status.code());
    }
}
