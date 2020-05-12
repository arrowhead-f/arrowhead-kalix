package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

public interface ArContractNegotiationTrustedSession {
    Future<TrustedSessionDto> getByNamesAndId(final String offerorName, final String receiverName, final long id);
}
