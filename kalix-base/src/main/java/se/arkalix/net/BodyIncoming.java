package se.arkalix.net;

import se.arkalix.io.buffer.BufferReader;
import se.arkalix.util.concurrent.Future;

import java.nio.file.Path;

/**
 * Handle used to consume the body of some {@link MessageIncoming incoming
 * message}.
 */
public interface BodyIncoming {
    /**
     * Requests that the incoming message body be buffered in full and then be
     * wrapped into a {@link BufferReader}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     * If the returned {@link Future} successfully resolves into a buffer, it
     * must be {@link BufferReader#close() closed} once no longer in use.
     *
     * @return Future completed with fully buffered binary reader.
     */
    Future<BufferReader> buffer();

    /**
     * Requests that the incoming message body be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist. If the {@code append}
     * parameter is {@code true}, the file is appended to rather than being
     * overwritten.
     * <p>
     * Using this method, or {@link #writeTo(Path)}, is the preferred way of
     * receiving data objects that are too large to handle in-memory. This as
     * received data is written directly to the target file as it is received,
     * rather than being buffered until all of it becomes available, as in the
     * case of {@link #buffer()}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param path   Path to file to contain incoming message body.
     * @param append If {@code true}, any existing file at {@code path} will
     *               not be overwritten, but have the incoming message body
     *               appended to it.
     * @return Future completed successfully with given path only if the
     * incoming message body is fully received and written to the file at that
     * path.
     * @throws IllegalStateException If the body has already been requested.
     * @throws NullPointerException  If {@code path} is {@code null}.
     */
    Future<?> writeTo(final Path path, boolean append);

    /**
     * Requests that the incoming message body be written to the file at the
     * specified file system path.
     * <p>
     * The file will be created if it does not exist, or overwritten if it does
     * exist.
     * <p>
     * Using this method, or {@link #writeTo(Path, boolean)}, is the preferred
     * way of receiving data objects that are too large to handle in-memory.
     * This as received data is written directly to the target file as it is
     * received, rather than being buffered until all of it becomes available,
     * as in the case of {@link #buffer()}.
     * <p>
     * Calling this method consumes the body associated with this message. Any
     * further attempts to consume the body will cause exceptions to be thrown.
     *
     * @param path Path to file to contain incoming message body.
     * @return Future completed successfully with given path only if the
     * incoming message body is fully received and written to the file at that
     * path.
     * @throws IllegalStateException If the body has already been requested.
     */
    default Future<?> writeTo(final Path path) {
        return writeTo(path, false);
    }
}
