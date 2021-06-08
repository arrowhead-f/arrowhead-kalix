package se.arkalix.net.http._internal;

import se.arkalix.codec.MediaType;
import se.arkalix.net.BodyOutgoing;
import se.arkalix.net.MessageCodecMisspecified;
import se.arkalix.net.http.HttpHeaderInvalid;
import se.arkalix.net.http.HttpOutgoing;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

@Internal
public abstract class DefaultHttpOutgoing<Self> implements HttpOutgoing<Self> {
    private BodyOutgoing body;
    private MediaType contentType;

    protected abstract Self self();

    @Override
    public Optional<MediaType> contentType() {
        if (contentType == null) {
            try {
                contentType = headers()
                    .getAs("content-type", MediaType::valueOf)
                    .orElse(null);
            }
            catch (final HttpHeaderInvalid exception) {
                throw new MessageCodecMisspecified(this, exception.value(), exception);
            }
        }
        return Optional.ofNullable(contentType);
    }

    @Override
    public Self contentType(final MediaType contentType) {
        headers().set("content-type", contentType.toString());
        this.contentType = contentType;
        return self();
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
