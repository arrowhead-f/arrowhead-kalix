package se.arkalix.internal.description;

import se.arkalix.description.SystemDescription;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.annotation.Internal;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;

@Internal
public class DefaultSystemDescription implements SystemDescription {
    private final String name;
    private final PublicKey publicKey;
    private final InetSocketAddress socketAddress;

    public DefaultSystemDescription(
        final String name,
        final PublicKey publicKey,
        final InetSocketAddress socketAddress
    ) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.publicKey = publicKey;
        this.socketAddress = Objects.requireNonNull(socketAddress, "Expected remoteSocketAddress");
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
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final SystemDescription that = (SystemDescription) other;
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
        return "DefaultSystemDescription{" +
            "name='" + name + '\'' +
            ", socketAddress=" + socketAddress +
            (publicKey != null
                ? ", publicKey='base64:" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + '\''
                : ", publicKey=null") +
            '}';
    }
}
