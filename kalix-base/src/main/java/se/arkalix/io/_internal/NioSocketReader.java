package se.arkalix.io._internal;

import se.arkalix.io.IoException;
import se.arkalix.io.SocketReader;
import se.arkalix.io.buffer.BufferWriter;

import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Objects;

public class NioSocketReader implements SocketReader {
    private final ScatteringByteChannel channel;

    public NioSocketReader(final ScatteringByteChannel channel) {
        this.channel = Objects.requireNonNull(channel, "channel");
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
