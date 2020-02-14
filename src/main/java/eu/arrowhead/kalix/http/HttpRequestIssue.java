package eu.arrowhead.kalix.http;

/**
 * Signifies what is wrong with some received HTTP request.
 * <p>
 * Note that this type is <i>never</i> used to describe internal problems
 * arising from request processing, but describes <i>issues with the requests
 * themselves</i>.
 */
public enum HttpRequestIssue {
    BODY_STRUCTURE_UNEXPECTED,
    BODY_SYNTAX_INCORRECT,
    BODY_TOO_LONG,
    ;

    public String getDescription() {
        switch (this) {
            case BODY_STRUCTURE_UNEXPECTED:
                return "Failed to parse request body; unexpected structure";

            case BODY_SYNTAX_INCORRECT:
                return "Failed to parse request body; syntax error encountered";

            case BODY_TOO_LONG:
                return "The length of the request body exceeds provider limit";
        }
        return "Unknown request body issue";
    }
}