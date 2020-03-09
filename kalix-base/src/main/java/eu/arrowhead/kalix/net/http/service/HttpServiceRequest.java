package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The head and body of an incoming HTTP request.
 */
public interface HttpServiceRequest
    extends HttpServiceRequestHead, HttpServiceRequestBody {

    /**
     * Creates a shallow copy of this {@code HttpServiceRequest} that contains
     * the given {@code pathParameters}.
     *
     * @param pathParameters Path parameters to include in request copy.
     * @return Copy of this object that includes given path parameters.
     */
    default HttpServiceRequest newWithPathParameters(final List<String> pathParameters) {
        final var self = this;
        return new HttpServiceRequest() {
            @Override
            public <R extends DataReadable> FutureProgress<? extends R> bodyAs(final Class<R> class_) {
                return self.bodyAs(class_);
            }

            @Override
            public FutureProgress<byte[]> bodyAsByteArray() {
                return self.bodyAsByteArray();
            }

            @Override
            public FutureProgress<? extends InputStream> bodyAsStream() {
                return self.bodyAsStream();
            }

            @Override
            public FutureProgress<String> bodyAsString() {
                return self.bodyAsString();
            }

            @Override
            public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
                return self.bodyTo(path, append);
            }

            @Override
            public EncodingDescriptor encoding() {
                return self.encoding();
            }

            @Override
            public HttpHeaders headers() {
                return self.headers();
            }

            @Override
            public HttpMethod method() {
                return self.method();
            }

            @Override
            public String path() {
                return self.path();
            }

            @Override
            public List<String> pathParameters() {
                return pathParameters;
            }

            @Override
            public Map<String, List<String>> queryParameters() {
                return self.queryParameters();
            }

            @Override
            public HttpRequester requester() {
                return self.requester();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }
        };
    }
}
