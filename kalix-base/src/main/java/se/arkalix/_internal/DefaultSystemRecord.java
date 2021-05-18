package se.arkalix._internal;

import se.arkalix.SystemRecord;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.annotation.Internal;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Internal
public class DefaultSystemRecord implements SystemRecord {
    private final String name;
    private final PublicKey publicKey;
    private final InetSocketAddress socketAddress;
    private final Map<String, String> metadata;

    public DefaultSystemRecord(
        final String name,
        final PublicKey publicKey,
        final InetSocketAddress socketAddress,
        final Map<String, String> metadata
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.publicKey = publicKey;
        this.socketAddress = Objects.requireNonNull(socketAddress, "remoteSocketAddress");
        this.metadata = metadata == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(metadata);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    @Override
    public boolean isSecure() {
        return publicKey != null;
    }

    @Override
    public PublicKey publicKey() {
        if (publicKey == null) {
            throw new SecurityDisabled("Not in secure mode");
        }
        return publicKey;
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final SystemRecord that = (SystemRecord) other;
        return name.equals(that.name()) &&
            socketAddress.equals(that.socketAddress()) &&
            isSecure() ?
            that.isSecure() && publicKey.equals(that.publicKey())
            : !that.isSecure();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, socketAddress, publicKey);
    }

    @Override
    public String toString() {
        return "DefaultSystemRecord{" +
            "name='" + name + '\'' +
            ", socketAddress=" + socketAddress +
            ", publicKey='base64:" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + '\'' +
            ", metadata=" + metadata +
            '}';
    }
}
