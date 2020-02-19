package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.concurrent.Future;

/**
 * Function useful for requesting a new or renewing an existing HTTP
 * authentication token.
 */
@FunctionalInterface
public interface HttpTokenRequester {
    /**
     * Requests new authentication token.
     *
     * @param token Existing authentication token, or {@code null}.
     * @return Future of requested {@link HttpToken}.
     */
    Future<HttpToken> request(final HttpToken token);
}
