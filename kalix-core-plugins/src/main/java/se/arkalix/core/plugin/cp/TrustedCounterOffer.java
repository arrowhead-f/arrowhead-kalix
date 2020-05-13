package se.arkalix.core.plugin.cp;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrustedCounterOffer {
    private final Instant validAfter;
    private final Instant validUntil;
    private final List<TrustedContractDto> contracts;
    private final Instant offeredAt;

    public static TrustedCounterOffer from(final TrustedContractDto... contracts) {
        return new Builder()
            .contracts(contracts)
            .build();
    }

    public static TrustedCounterOffer from(final List<TrustedContractDto> contracts) {
        return new Builder()
            .contracts(contracts)
            .build();
    }

    private TrustedCounterOffer(final Builder builder) {
        offeredAt = Instant.now();
        validAfter = Objects.requireNonNullElse(builder.validAfter, offeredAt);
        validUntil = Objects.requireNonNullElseGet(builder.validUntil, () ->
            validAfter.plus(ArContractProxyConstants.DEFAULT_OFFER_VALIDITY_PERIOD));
        contracts = builder.contracts == null || builder.contracts.size() == 0 ? Collections.emptyList() : Collections.unmodifiableList(builder.contracts);

    }

    public Instant validAfter() {
        return validAfter;
    }

    public Instant validUntil() {
        return validUntil;
    }

    public List<TrustedContractDto> contracts() {
        return contracts;
    }

    public Instant offeredAt() {
        return offeredAt;
    }

    public static class Builder {
        Instant validAfter;

        Instant validUntil;

        List<TrustedContractDto> contracts;

        public Builder validAfter(final Instant validAfter) {
            this.validAfter = validAfter;
            return this;
        }

        public Builder validUntil(final Instant validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder contracts(final List<TrustedContractDto> contracts) {
            this.contracts = contracts;
            return this;
        }

        public Builder contracts(final TrustedContractDto... contracts) {
            this.contracts = Arrays.asList(contracts);
            return this;
        }

        public TrustedCounterOffer build() {
            return new TrustedCounterOffer(this);
        }
    }
}
