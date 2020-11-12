package se.arkalix.core.plugin._internal;

import se.arkalix.core.plugin.ErrorResponseDto;
import se.arkalix.core.plugin.ErrorResponseException;
import se.arkalix.codec.Decoder;
import se.arkalix.net.http.HttpIncomingResponse;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;
import java.util.Optional;

@Internal
public class HttpJsonServices {
    public static Future<?> unwrap(final HttpIncomingResponse<?, ?> response) {
        Objects.requireNonNull(response, "response");

        if (response.status().isSuccess()) {
            return Future.done();
        }
        return handleError(response);
    }

    public static <T> Future<T> unwrap(final HttpIncomingResponse<?, ?> response, final Decoder<T> decoder) {
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(decoder, "decoder");

        if (response.status().isSuccess()) {
            return response.bodyTo(decoder);
        }
        return handleError(response);
    }

    public static <T> Future<Optional<T>> unwrapOptional(
        final HttpIncomingResponse<?, ?> response,
        final Decoder<T> decoder)
    {
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(decoder, "decoder");

        final var status = response.status();
        if (status.isSuccess()) {
            return response.bodyTo(decoder)
                .map(Optional::of);
        }
        if (status == HttpStatus.NOT_FOUND) {
            return Future.success(Optional.empty());
        }
        return handleError(response);
    }

    private static <T> Future<T> handleError(final HttpIncomingResponse<?, ?> response) {
        if (response.status().isClientError()) {
            final var headers = response.headers();
            if (headers.get("content-type").orElse("").startsWith("application/json")) {
                return response.bodyTo(ErrorResponseDto::decode)
                    .mapThrow(error -> new ErrorResponseException(response, error));
            }
        }
        return Future.failure(response.reject());
    }
}
