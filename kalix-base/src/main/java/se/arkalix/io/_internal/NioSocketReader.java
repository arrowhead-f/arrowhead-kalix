package se.arkalix.io._internal;

import se.arkalix.io.IoException;
import se.arkalix.io.SocketReader;
import se.arkalix.io.buffer.BufferWriter;

import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Objects;

public record NioSocketReader(ScatteringByteChannel channel) implements SocketReader {
    public NioSocketReader {
        Objects.requireNonNull(channel);
    }

    @Override
    public void close() {
        try {
            channel.close();
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }

    @Override
    public boolean isClosed() {
        return !channel.isOpen();
    }

    @Override
    public int read(final BufferWriter destination) {
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        return destination.write(channel);
    }
}
