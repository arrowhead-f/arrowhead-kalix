package eu.arrowhead.kalix.http;

/**
 * Signifies that some HTTP request contains an error and, therefore, will not
 * be processed.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP request causes an error that should generate a stack
 * trace, some other exception should be used instead.
 */
public class HttpRequestException extends Exception {
    private final HttpRequestIssue issue;

    private HttpRequestException(final HttpRequestIssue issue) {
        super(null, null, false, false); // Disable stack trace.
        this.issue = issue;
    }

    /**
     * Creates new {@link HttpRequestException} from an issue description.
     *
     * @param issue The reason the request in question is invalid.
     * @return New {@link HttpRequestException}.
     */
    public HttpRequestException fromIssue(final HttpRequestIssue issue) {
        return new HttpRequestException(issue);
    }

    /**
     * @return The issue causing this exception to be thrown.
     */
    public HttpRequestIssue getIssue() {
        return issue;
    }

    @Override
    public String getMessage() {
        return issue.getDescription();
    }
}
