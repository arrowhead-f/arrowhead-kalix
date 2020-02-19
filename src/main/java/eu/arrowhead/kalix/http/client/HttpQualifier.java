package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.concurrent.Future;

/**
 * A {@link HttpClient} qualifier, verifying and/or modifying outgoing HTTP
 * requests.
 */
@FunctionalInterface
public interface HttpQualifier {
    /**
     * Called to qualify given {@code HttpClientRequest}.
     * <p>
     * Qualification may entail ensuring certain constraints are not violated,
     * adding authorization headers, proxy headers, and so on.
     * <p>
     * Note that the given request <i>may</i> be modified by this function. The
     * function returns a {@code Future} that is completed when that
     * modification is done. Whoever wishes to use the request should wait
     * until after the returned {@code Future} completes successfully.
     *
     * @param request Request to qualify.
     * @return Future of qualification completion.
     */
    Future<Void> qualify(HttpClientRequest request);
}
