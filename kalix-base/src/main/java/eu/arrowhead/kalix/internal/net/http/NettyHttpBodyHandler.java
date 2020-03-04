package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.io.InputStream;
import java.nio.file.Path;

public interface NettyHttpBodyHandler {
    <R extends DataReadable> Future<R> bodyAs(final Class<R> class_);

    Future<byte[]> bodyAsBytes();

    InputStream bodyAsStream();

    Future<String> bodyAsString();

    Future<?> bodyToPath(final Path path);
}
