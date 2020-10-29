package se.arkalix.net._internal;

import se.arkalix.encoding.Encoding;
import se.arkalix.net.BodyOutgoing;
import se.arkalix.net.MessageOutgoing;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

@Internal
public abstract class DefaultMessageOutgoing<Self> implements MessageOutgoing<Self> {
    private BodyOutgoing body;
    private Encoding encoding;

    protected abstract Self self();

    protected Self encoding(final Encoding encoding) {
        this.encoding = encoding;
        return self();
    }

    @Override
    public Optional<Encoding> encoding() {
        return Optional.ofNullable(encoding);
    }

    @Override
    public Optional<BodyOutgoing> body() {
        return Optional.ofNullable(body);
    }

    @Override
    public Self body(final BodyOutgoing body) {
        this.body = body;
        return self();
    }
}
