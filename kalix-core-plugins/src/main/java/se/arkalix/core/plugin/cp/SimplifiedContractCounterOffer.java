package se.arkalix.core.plugin.cp;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A contract counter-offer useful when important details about the negotiation
 * in question can be inferred.
 */
@SuppressWarnings("unused")
public class SimplifiedContractCounterOffer {
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
    public static SimplifiedContractCounterOffer from(final TrustedContractDto... contracts) {
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
    public static SimplifiedContractCounterOffer from(final List<TrustedContractDto> contracts) {
        return new Builder()
            .contracts(contracts)
            .build();
    }

    private SimplifiedContractCounterOffer(final Builder builder) {
        offeredAt = Instant.now();
        validAfter = Objects.requireNonNullElse(builder.validAfter, offeredAt);
        validUntil = Objects.requireNonNull(builder.validUntil, "Expected validUntil");
        Objects.requireNonNull(builder.contracts, "Expected contracts");
        if (builder.contracts.isEmpty()) {
            throw new IllegalArgumentException("Expected contracts.size() > 0");
        }
        contracts = Collections.unmodifiableList(builder.contracts);
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
     * Duration until this counter-offer can no longer be accepted or rejected.
     */
    public Duration expiresIn() {
        return Duration.between(Instant.now(), validUntil);
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
     * Builder useful for creating {@link SimplifiedContractCounterOffer} instances.
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
         * accepted or rejected by adding given {@code duration} to whatever
         * time was provided to {@link #validAfter()}. <b>Must be specified.</b>
         * <p>
         * If no time has been provided to {@link #validAfter()} when this
         * method is called, it is automatically set to the current time.
         *
         * @param duration The amount of time for which the receiver of this
         *                 counter-offer can accept, counter or reject it.
         * @return This builder.
         */
        public Builder validFor(final Duration duration) {
            if (validAfter == null) {
                validAfter = Instant.now();
            }
            this.validUntil = duration != null ? validAfter.plus(duration) : null;
            return this;
        }

        /**
         * Sets instant after which the created counter-offer can no longer be
         * accepted or rejected. <b>Must be specified.</b>
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
         * Sets offered contracts. <b>At least one must be specified.</b>
         *
         * @param contracts Offered contracts.
         * @return This builder.
         */
        public Builder contracts(final List<TrustedContractDto> contracts) {
            this.contracts = contracts;
            return this;
        }

        /**
         * Sets offered contracts. <b>At least one must be specified.</b>
         *
         * @param contracts Offered contracts.
         * @return This builder.
         */
        public Builder contracts(final TrustedContractDto... contracts) {
            this.contracts = Arrays.asList(contracts);
            return this;
        }

        /**
         * Finalizes construction of {@link SimplifiedContractCounterOffer}.
         *
         * @return New {@link SimplifiedContractCounterOffer}.
         */
        public SimplifiedContractCounterOffer build() {
            return new SimplifiedContractCounterOffer(this);
        }
    }
}
