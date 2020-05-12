package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

public interface ArTrustedNegotiationResponder {
    Future<?> accept();

    Future<?> offer(TrustedCounterOffer offer);

    Future<?> reject();
}
