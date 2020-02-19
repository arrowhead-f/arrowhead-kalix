package eu.arrowhead.kalix.http;

import java.util.Map;
import java.util.Optional;

/**
 * A outgoing HTTP response, to be sent by a {@link HttpService}.
 * <p>
 * This class gives no opportunity for modifying any body sent in the response.
 * Bodies are set by returning a non-null value from a
 * {@link HttpServiceHandler}.
 */
public class HttpServiceResponse {
    /**
     * Schedules request response for cancellation.
     * <p>
     * No more {@link HttpServiceHandler}s are invoked if this method is
     * called, and no response is sent to the requesting system.
     */
    public void cancel() {
        // TODO.
    }

    /**
     * Schedules request response for cancellation.
     * <p>
     * No more {@link HttpServiceHandler}s are invoked if this method is
     * called. An HTTP response with given {@code status} is sent to the sender
     * on a best-effort basis. No body will be present in the response, even if
     * one would be returned from any {@link HttpServiceHandler} calling this
     * method.
     *
     * @param status Status in sent HTTP response.
     */
    public void cancel(final HttpStatus status) {
        cancel(status, null);
    }

    /**
     * Schedules request response for cancellation.
     * <p>
     * No more {@link HttpServiceHandler}s are invoked if this method is
     * called. An HTTP response with given {@code status} is sent to the sender
     * on a best-effort basis. The provided message will be provided in the
     * body of the response, even if another body would be returned from any
     * {@link HttpServiceHandler} calling this method.
     *
     * @param status  Status in sent HTTP response.
     * @param message Message to provide in HTTP response body, if any.
     */
    public void cancel(final HttpStatus status, final String message) {
        // TODO.
    }

    /**
     * Gets a response header value by name.
     *
     * @param name Name of header. Case sensitive. Should be lower-case.
     * @return Header value, if any.
     */
    public Optional<String> header(final String name) {
        return null;
    }

    /**
     * Sets a response header.
     *
     * @param name  Name of header. Case sensitive. Should be lower-case.
     * @param value Header value.
     * @return This response object.
     */
    public HttpServiceResponse header(final String name, final String value) {
        // TODO.
        return this;
    }

    /**
     * @return Mutable {@link Map} of all response headers.
     */
    public Map<String, String> headers() {
        return null;
    }

    /**
     * Replaces all existing response headers.
     *
     * @param headers New map of response headers.
     * @return This response object.
     */
    public HttpServiceResponse headers(final Map<String, String> headers) {
        return null;
    }

    /**
     * @return Currently set response {@link HttpStatus}.
     */
    public HttpStatus status() {
        return null;
    }

    /**
     * @param status New response {@link HttpStatus}.
     * @return This response object.
     */
    public HttpServiceResponse status(final HttpStatus status) {
        // TODO.
        return this;
    }
}
