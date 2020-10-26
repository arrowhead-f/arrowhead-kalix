package se.arkalix.net._internal;

import se.arkalix.net.Encoding;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoWritable;
import se.arkalix.net.MessageOutgoing;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class DefaultMessageOutgoing<Self> implements MessageOutgoing<Self> {
    private Object body;
    private Charset charset;
    private Encoding encoding;

    protected abstract Self self();

    @Override
    public Optional<Charset> charset() {
        return Optional.ofNullable(charset);
    }

    @Override
    public Optional<Encoding> encoding() {
        return Optional.ofNullable(encoding);
    }

    @Override
    public Optional<Object> body() {
        return Optional.ofNullable(body);
    }

    @Override
    public Self body(final byte[] byteArray) {
        body = byteArray;
        charset = null;
        encoding = null;
        return self();
    }

    @Override
    public Self body(final DtoEncoding encoding, final DtoWritable data) {
        return bodyUnsafe(encoding, data);
    }

    @Override
    public <L extends List<? extends DtoWritable>> Self body(final DtoEncoding encoding, final L data) {
        return bodyUnsafe(encoding, data);
    }

    protected Self bodyUnsafe(final DtoEncoding encoding, final Object data) {
        charset = null;
        this.encoding = encoding != null ? Encoding.get(encoding) : null;
        body = data;

        return self();
    }

    @Override
    public Self body(final Path path) {
        body = path;
        charset = null;
        encoding = null;
        return self();
    }

    @Override
    public Self body(final Charset charset, final String string) {
        body = string;
        this.charset = charset;
        encoding = null;
        return self();
    }

    @Override
    public Self clearBody() {
        body = null;
        charset = null;
        encoding = null;
        return self();
    }
}
