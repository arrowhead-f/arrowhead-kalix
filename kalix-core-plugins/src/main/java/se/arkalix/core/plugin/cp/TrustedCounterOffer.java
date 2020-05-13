package se.arkalix.core.plugin.cp;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A limited version of {@link TrustedOffer} useful when information about
 * the negotiating parties can be inferred.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
public class TrustedCounterOffer {
    private final Instant validAfter;
    private final Instant validUntil;
    private final List<TrustedContractDto> contracts;
    private final Instant offeredAt;

    /**
     * Creates new counter-offer from given contracts.
     *
     * @param contracts Offered contracts.
     * @return New counter-offer.
     */
    public static TrustedCounterOffer from(final TrustedContractDto... contracts) {
        return new Builder()
            .contracts(contracts)
            .build();
    }

    /**
     * Creates new counter-offer from given contracts.
     *
     * @param contracts Offered contracts.
     * @return New counter-offer.
     */
    public static TrustedCounterOffer from(final List<TrustedContractDto> contracts) {
        return new Builder()
            .contracts(contracts)
            .build();
    }

    private TrustedCounterOffer(final Builder builder) {
        offeredAt = Instant.now();
        validAfter = Objects.requireNonNullElse(builder.validAfter, offeredAt);
        validUntil = Objects.requireNonNullElseGet(builder.validUntil, () ->
            validAfter.plus(ArContractNegotiationConstants.DEFAULT_OFFER_VALIDITY_PERIOD));
        contracts = builder.contracts == null || builder.contracts.size() == 0 ? Collections.emptyList() : Collections.unmodifiableList(builder.contracts);

    }

    /**
     * Instant after which this counter-offer becomes acceptable.
     */
    public Instant validAfter() {
        return validAfter;
    }

    /**
     * Instant until this counter-offer can be accepted or rejected.
     */
    public Instant validUntil() {
        return validUntil;
    }

    /**
     * Offered contracts.
     */
    public List<TrustedContractDto> contracts() {
        return contracts;
    }

    /**
     * The time at which this counter-offer was created.
     */
    public Instant offeredAt() {
        return offeredAt;
    }

    /**
     * Builder useful for creating {@link TrustedCounterOffer} instances.
     */
    @SuppressWarnings("unused")
    public static class Builder {
        private Instant validAfter;
        private Instant validUntil;
        private List<TrustedContractDto> contracts;

        /**
         * Sets instant after which the created counter-offer becomes
         * acceptable.
         *
         * @param validAfter Instant after which the created counter-offer
         *                   becomes acceptable.
         * @return This builder.
         */
        public Builder validAfter(final Instant validAfter) {
            this.validAfter = validAfter;
            return this;
        }

        /**
         * Sets instant after which the created counter-offer can no longer be
         * accepted or rejected.
         *
         * @param validUntil Instant after which the created counter-offer can
         *                   no longer be accepted or rejected.
         * @return This builder.
         */
        public Builder validUntil(final Instant validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        /**
         * Sets offered contracts.
         *
         * @param contracts Offered contracts.
         * @return This builder.
         */
        public Builder contracts(final List<TrustedContractDto> contracts) {
            this.contracts = contracts;
            return this;
        }

        /**
         * Sets offered contracts.
         *
         * @param contracts Offered contracts.
         * @return This builder.
         */
        public Builder contracts(final TrustedContractDto... contracts) {
            this.contracts = Arrays.asList(contracts);
            return this;
        }

        /**
         * Finalizes construction of {@link TrustedCounterOffer}.
         *
         * @return New {@link TrustedCounterOffer}.
         */
        public TrustedCounterOffer build() {
            return new TrustedCounterOffer(this);
        }
    }
}
