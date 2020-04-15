package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.Error;
import se.arkalix.core.plugin.dto.ErrorDto;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.client.HttpClientResponseException;
import se.arkalix.net.http.consumer.HttpConsumerResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

import static se.arkalix.dto.DtoEncoding.JSON;

class HttpJsonServices {
    public static <T extends DtoReadable> Future<T> unwrap(final HttpConsumerResponse response, final Class<T> class_) {
        Objects.requireNonNull(response, "Expected response");

        final var status = response.status();

        if (status.isSuccess()) {
            return class_ != null ? response.bodyAs(class_) : Future.done();
        }

        if (status.isClientError()) {
            final var headers = response.headers();
            if (headers.getAsInteger("content-length").orElse(0) > 0) {
                if (headers.get("content-type").orElse("").startsWith("application/json")) {
                    return response.bodyAs(JSON, ErrorDto.class)
                        .mapThrow(Error::toException);
                }
                return response.bodyAsString()
                    .mapThrow(HttpClientResponseException::new);
            }
        }

        return Future.failure(response.reject());
    }
}
