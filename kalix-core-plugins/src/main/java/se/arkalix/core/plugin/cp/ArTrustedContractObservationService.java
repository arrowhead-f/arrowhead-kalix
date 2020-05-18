package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

import java.util.Optional;

/**
 * A service useful for querying the state of some on-going or previously
 * closed negotiation {@link TrustedContractNegotiation session}.
 */
public interface ArTrustedContractObservationService {
    /**
     * Requests some {@link TrustedContractNegotiation negotiation session}
     * uniquely identified by the names of the two negotiating parties and an
     * identifier.
     *
     * @param name1 Name of the one party.
     * @param name2 Name of the other party.
     * @param id    Negotiation session identifier.
     * @return Current negotiation state, if any such is associated with the
     * given names and identifier.
     */
    Future<Optional<TrustedContractNegotiationDto>> getByNamesAndId(
        final String name1,
        final String name2,
        final long id);
}
