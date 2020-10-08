package se.arkalix.net.http;

import se.arkalix.net.Message;
import se.arkalix.net.MessageException;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

/**
 * Signifies that some outgoing HTTP request is subject to an error.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpOutgoingRequestException extends MessageException {
    private final HttpOutgoingRequest<?> request;

    /**
     * Creates new HTTP response exception with given message.
     *
     * @param request Request that could not be fulfilled due to the condition
     *                described by this exception occurring.
     * @param message Human-readable description of issue.
     */
    public HttpOutgoingRequestException(final HttpOutgoingRequest<?> request, final String message) {
        this(request, request, message, null);
    }

    protected HttpOutgoingRequestException(
        final Message message,
        final HttpOutgoingRequest<?> request,
        final String description,
        final Throwable cause
    ) {
        super(message, description, cause);
        this.request = request;
    }

    /**
     * Gets request associated with this response exception.
     *
     * @return Request that could not be fulfilled due to the condition
     * described by this exception occurring.
     */
    public HttpOutgoingRequest<?> request() {
        return request;
    }

    @Override
    public String getMessage() {
        return format() + " " + super.getMessage();
    }

    protected String format() {
        final var request = request();
        return "[" +
            request.method().map(Object::toString).orElse("<method?>") + " " +
            "//" + request.header(HOST).orElse("<host?>") +
            request.path().orElse("<path?>") +
            "]";
    }
}
