package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

import java.util.Optional;

/**
 * A service useful for querying the state of some on-going or previously
 * closed negotiation {@link TrustedSession session}.
 */
public interface ArContractNegotiationTrustedSession {
    /**
     * Requests some negotiation {@link TrustedSession session} uniquely
     * identified by the names of two parties and a session identifier.
     *
     * @param name1 Name of the one party.
     * @param name2 Name of the other party.
     * @param id    Session identifier.
     * @return Current session state, if any such is associated with the given
     * names and identifiers.
     */
    Future<Optional<TrustedSessionDto>> getByNamesAndId(
        final String name1,
        final String name2,
        final long id);
}
