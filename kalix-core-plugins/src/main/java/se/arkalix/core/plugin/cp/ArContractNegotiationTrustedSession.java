package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

import java.util.Optional;

/**
 * A service useful for querying the state of some on-going or previously
 * closed negotiation {@link TrustedContractSession session}.
 */
public interface ArContractNegotiationTrustedSession {
    /**
     * Requests some negotiation {@link TrustedContractSession session}
     * uniquely identified by the names of the two negotiating parties and a
     * session identifier.
     *
     * @param name1 Name of the one party.
     * @param name2 Name of the other party.
     * @param id    Session identifier.
     * @return Current session state, if any such is associated with the given
     * names and identifiers.
     */
    Future<Optional<TrustedContractSessionDto>> getByNamesAndId(final String name1, final String name2, final long id);
}
