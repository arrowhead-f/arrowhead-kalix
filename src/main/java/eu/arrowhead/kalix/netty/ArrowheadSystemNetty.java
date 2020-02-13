package eu.arrowhead.kalix.netty;

import eu.arrowhead.kalix.ArrowheadClient;
import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.concurrent.Future;

public final class ArrowheadSystemNetty extends ArrowheadSystem {
    protected ArrowheadSystemNetty(final Builder builder) {
        super(builder);
    }

    public Future<ArrowheadClient> start() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public static final class Builder extends ArrowheadSystem.Builder {

        @Override
        public ArrowheadSystem build() {
            return new ArrowheadSystemNetty(this);
        }
    }
}
