package se.arkalix.core.plugin.cp;

@FunctionalInterface
public interface ArTrustedNegotiationObserver {
    void onUpdate(final TrustedSessionStatus status, final TrustedSessionCandidate candidate);
}
