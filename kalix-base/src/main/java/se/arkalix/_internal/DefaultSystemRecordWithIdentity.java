package se.arkalix._internal;

import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.annotation.Internal;

import java.net.InetSocketAddress;
import java.util.Objects;

@Internal
public class DefaultSystemRecordWithIdentity implements SystemRecordWithIdentity {
    private final String name;
    private final SystemIdentity identity;
    private final InetSocketAddress socketAddress;

    public DefaultSystemRecordWithIdentity(
        final String name,
        final SystemIdentity identity,
        final InetSocketAddress socketAddress
    ) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.identity = identity;
        this.socketAddress = Objects.requireNonNull(socketAddress, "Expected socketAddress");

    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public SystemIdentity identity() {
        if (identity == null) {
            throw new SecurityDisabled("Not in secure mode");
        }
        return identity;
    }

    @Override
    public boolean isSecure() {
        return identity != null;
    }

    @Override
    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final SystemRecordWithIdentity that = (SystemRecordWithIdentity) other;
        return name.equals(that.name()) &&
            socketAddress.equals(that.socketAddress()) &&
            isSecure()
                ? that.isSecure() && identity.equals(that.identity())
                : !that.isSecure();
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, name, socketAddress);
    }

    @Override
    public String toString() {
        return "DefaultPeerDescription{" +
            "name=" + name +
            ", identity='" + identity + '\'' +
            ", socketAddress=" + socketAddress +
            '}';
    }
}
