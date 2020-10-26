package se.arkalix.core.plugin._internal;

import se.arkalix.core.plugin.ErrorResponseDto;
import se.arkalix.core.plugin.ErrorResponseException;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.HttpIncomingResponse;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

@Internal
public class HttpJsonServices {
    public static Future<?> unwrap(final HttpIncomingResponse<?, ?> response) {
        Objects.requireNonNull(response, "Expected response");

        if (response.status().isSuccess()) {
            return Future.done();
        }
        return handleError(response);
    }

    public static <T extends DtoReadable> Future<T> unwrap(final HttpIncomingResponse<?, ?> response, final Class<T> class_) {
        Objects.requireNonNull(response, "Expected response");
        Objects.requireNonNull(class_, "Expected class");

        if (response.status().isSuccess()) {
            return response.bodyAs(class_);
        }
        return handleError(response);
    }

    public static <T extends DtoReadable> Future<Optional<T>> unwrapOptional(
        final HttpIncomingResponse<?, ?> response,
        final Class<T> class_)
    {
        Objects.requireNonNull(response, "Expected response");
        Objects.requireNonNull(class_, "Expected class_");

        final var status = response.status();
        if (status.isSuccess()) {
            return response.bodyAs(class_)
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
                return response.bodyAs(JSON, ErrorResponseDto.class)
                    .mapThrow(error -> new ErrorResponseException(response, error));
            }
        }
        return Future.failure(response.reject());
    }
}
