package eu.arrowhead.kalix.net.http;

import java.util.Objects;

/**
 * HTTP status codes.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2295">RFC 2295</a>
 * @see <a href="https://tools.ietf.org/html/rfc2324">RFC 2324</a>
 * @see <a href="https://tools.ietf.org/html/rfc2518">RFC 2518</a>
 * @see <a href="https://tools.ietf.org/html/rfc2774">RFC 2774</a>
 * @see <a href="https://tools.ietf.org/html/rfc3229">RFC 3229</a>
 * @see <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a>
 * @see <a href="https://tools.ietf.org/html/rfc5842">RFC 5842</a>
 * @see <a href="https://tools.ietf.org/html/rfc6585">RFC 6585</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>
 * @see <a href="https://tools.ietf.org/html/rfc7232">RFC 7232</a>
 * @see <a href="https://tools.ietf.org/html/rfc7233">RFC 7233</a>
 * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7235</a>
 * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
 */
public class HttpStatus {
    private final int code;
    private final boolean isStandard;
    private final String text;

    private HttpStatus(final int code, final boolean isStandard, final String text) {
        this.code = code;
        this.isStandard = isStandard;
        this.text = text;
    }

    /**
     * @return Status code.
     */
    public int code() {
        return code;
    }

    /**
     * @return Status code and canonical reason, unless status is non-standard,
     * in which case only a status code is present in the returned string.
     */
    public String text() {
        return text;
    }

    /**
     * @return Whether status is standardized.
     */
    public boolean isStandard() {
        return isStandard;
    }

    /**
     * @return Whether status code is within 100..199.
     */
    public boolean isInformational() {
        return this.code >= 100 && this.code <= 199;
    }

    /**
     * @return Whether status code is within 200..299.
     */
    public boolean isSuccess() {
        return this.code >= 200 && this.code <= 299;
    }

    /**
     * @return Whether status code is within 300..399.
     */
    public boolean isRedirection() {
        return this.code >= 300 && this.code <= 399;
    }

    /**
     * @return Whether status code is within 400..499.
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code <= 499;
    }

    /**
     * @return Whether status code is within 500..599.
     */
    public boolean isServerError() {
        return this.code >= 500 && this.code <= 599;
    }

    /**
     * Status {@code 100 Continue}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">RFC 7231, Section 6.2.1</a>
     */
    public static final HttpStatus CONTINUE = new HttpStatus(100, true, "100 Continue");

    /**
     * Status {@code 101 Switching Protocols}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">RFC 7231, Section 6.2.2</a>
     */
    public static final HttpStatus SWITCHING_PROTOCOLS = new HttpStatus(101, true, "101 Switching Protocols");

    /**
     * Status {@code 102 Processing}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2518">RFC 2518</a>
     */
    public static final HttpStatus PROCESSING = new HttpStatus(102, true, "102 Processing");

    /**
     * Status {@code 200 OK}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">RFC 7231, Section 6.3.1</a>
     */
    public static final HttpStatus OK = new HttpStatus(200, true, "200 OK");

    /**
     * Status {@code 201 Created}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">RFC 7231, Section 6.3.2</a>
     */
    public static final HttpStatus CREATED = new HttpStatus(201, true, "201 Created");

    /**
     * Status {@code 202 Accepted}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">RFC 7231, Section 6.3.3</a>
     */
    public static final HttpStatus ACCEPTED = new HttpStatus(202, true, "202 Accepted");

    /**
     * Status {@code 203 Non-Authoritative Information}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">RFC 7231, Section 6.3.4</a>
     */
    public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = new HttpStatus(203, true, "203 Non Authoritative " +
        "Information");

    /**
     * Status {@code 204 No Content}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">RFC 7231, Section 6.3.5</a>
     */
    public static final HttpStatus NO_CONTENT = new HttpStatus(204, true, "204 No Content");

    /**
     * Status {@code 205 Reset Content}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">RFC 7231, Section 6.3.6</a>
     */
    public static final HttpStatus RESET_CONTENT = new HttpStatus(205, true, "205 Reset Content");

    /**
     * Status {@code 206 Partial Content}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">RFC 7233, Section 4.1</a>
     */
    public static final HttpStatus PARTIAL_CONTENT = new HttpStatus(206, true, "206 Partial Content");

    /**
     * Status {@code 207 Multi-Status}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a>
     */
    public static final HttpStatus MULTI_STATUS = new HttpStatus(207, true, "207 Multi-Status");

    /**
     * Status {@code 208 Already Reported}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc5842">RFC 5842</a>
     */
    public static final HttpStatus ALREADY_REPORTED = new HttpStatus(208, true, "208 Already Reported");

    /**
     * Status {@code 226 IM Used}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3229">RFC 3229</a>
     */
    public static final HttpStatus IM_USED = new HttpStatus(226, true, "226 IM Used");

    /**
     * Status {@code 300 Multiple Choices}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">RFC 7231, Section 6.4.1</a>
     */
    public static final HttpStatus MULTIPLE_CHOICES = new HttpStatus(300, true, "300 Multiple Choices");

    /**
     * Status {@code 301 Moved Permanently}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">RFC 7231, Section 6.4.2</a>
     */
    public static final HttpStatus MOVED_PERMANENTLY = new HttpStatus(301, true, "301 Moved Permanently");

    /**
     * Status {@code 302 Found}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">RFC 7231, Section 6.4.3</a>
     */
    public static final HttpStatus FOUND = new HttpStatus(302, true, "302 Found");

    /**
     * Status {@code 303 See Other}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">RFC 7231, Section 6.4.4</a>
     */
    public static final HttpStatus SEE_OTHER = new HttpStatus(303, true, "303 See Other");

    /**
     * Status {@code 304 Not Modified}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">RFC 7232, Section 4.1</a>
     */
    public static final HttpStatus NOT_MODIFIED = new HttpStatus(304, true, "304 Not Modified");

    /**
     * Status {@code 305 Use Proxy}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">RFC 7231, Section 6.4.5</a>
     */
    public static final HttpStatus USE_PROXY = new HttpStatus(305, true, "305 Use Proxy");

    /**
     * Status {@code 307 Temporary Redirect}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">RFC 7231, Section 6.4.7</a>
     */
    public static final HttpStatus TEMPORARY_REDIRECT = new HttpStatus(307, true, "307 Temporary Redirect");

    /**
     * Status {@code 308 Permanent Redirect}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
     */
    public static final HttpStatus PERMANENT_REDIRECT = new HttpStatus(308, true, "308 Permanent Redirect");

    /**
     * Status {@code 400 Bad Request}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">RFC 7231, Section 6.5.1</a>
     */
    public static final HttpStatus BAD_REQUEST = new HttpStatus(400, true, "400 Bad Request");

    /**
     * Status {@code 401 Unauthorized}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">RFC 7235, Section 3.1</a>
     */
    public static final HttpStatus UNAUTHORIZED = new HttpStatus(401, true, "401 Unauthorized");

    /**
     * Status {@code 402 Payment Required}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">RFC 7231, Section 6.5.2</a>
     */
    public static final HttpStatus PAYMENT_REQUIRED = new HttpStatus(402, true, "402 Payment Required");

    /**
     * Status {@code 403 Forbidden}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">RFC 7231, Section 6.5.3</a>
     */
    public static final HttpStatus FORBIDDEN = new HttpStatus(403, true, "403 Forbidden");

    /**
     * Status {@code 404 Not Found}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">RFC 7231, Section 6.5.4</a>
     */
    public static final HttpStatus NOT_FOUND = new HttpStatus(404, true, "404 Not Found");

    /**
     * Status {@code 405 Method Not Allowed}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">RFC 7231, Section 6.5.5</a>
     */
    public static final HttpStatus METHOD_NOT_ALLOWED = new HttpStatus(405, true, "405 Method Not Allowed");

    /**
     * Status {@code 406 Not Acceptable}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">RFC 7231, Section 6.5.6</a>
     */
    public static final HttpStatus NOT_ACCEPTABLE = new HttpStatus(406, true, "406 Not Acceptable");

    /**
     * Status {@code 407 Proxy Authentication Required}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">RFC 7235, Section 3.2</a>
     */
    public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = new HttpStatus(407, true, "407 Proxy " +
        "Authentication Required");

    /**
     * Status {@code 408 Request Timeout}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">RFC 7231, Section 6.5.7</a>
     */
    public static final HttpStatus REQUEST_TIMEOUT = new HttpStatus(408, true, "408 Request Timeout");

    /**
     * Status {@code 409 Conflict}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">RFC 7231, Section 6.5.8</a>
     */
    public static final HttpStatus CONFLICT = new HttpStatus(409, true, "409 Conflict");

    /**
     * Status {@code 410 Gone}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9">RFC 7231, Section 6.5.9</a>
     */
    public static final HttpStatus GONE = new HttpStatus(410, true, "410 Gone");

    /**
     * Status {@code 411 Length Required}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10">RFC 7231, Section 6.5.10</a>
     */
    public static final HttpStatus LENGTH_REQUIRED = new HttpStatus(411, true, "411 Length Required");

    /**
     * Status {@code 412 Precondition Failed}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2">RFC 7232, Section 4.2</a>
     */
    public static final HttpStatus PRECONDITION_FAILED = new HttpStatus(412, true, "412 Precondition Failed");

    /**
     * Status {@code 413 Payload Too Large}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11">RFC 7231, Section 6.5.11</a>
     */
    public static final HttpStatus PAYLOAD_TOO_LARGE = new HttpStatus(413, true, "413 Payload Too Large");

    /**
     * Status {@code 414 URI Too Long}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12">RFC 7231, Section 6.5.12</a>
     */
    public static final HttpStatus URI_TOO_LONG = new HttpStatus(414, true, "414 URI Too Long");

    /**
     * Status {@code 415 Unsupported Media Type}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">RFC 7231, Section 6.5.13</a>
     */
    public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = new HttpStatus(415, true, "415 Unsupported Media Type");

    /**
     * Status {@code 416 Range Not Satisfiable}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">RFC 7233, Section 4.4</a>
     */
    public static final HttpStatus RANGE_NOT_SATISFIABLE = new HttpStatus(416, true, "416 Range Not Satisfiable");

    /**
     * Status {@code 417 Expectation Failed}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14">RFC 7231, Section 6.5.14</a>
     */
    public static final HttpStatus EXPECTATION_FAILED = new HttpStatus(417, true, "417 Expectation Failed");

    /**
     * Status {@code 418 I'm a teapot}.
     * <p>
     * Not registered by IANA. Use is not advised.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2324">RFC 2324</a>
     */
    public static final HttpStatus IM_A_TEAPOT = new HttpStatus(418, true, "418 I'm a teapot");

    /**
     * Status {@code 421 Misdirected Request}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc7540#section-9.1.2">RFC 7540, Section 9.1.2</a>
     */
    public static final HttpStatus MISDIRECTED_REQUEST = new HttpStatus(421, true, "421 Misdirected Request");

    /**
     * Status {@code 422 Unprocessable Entity}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a>
     */
    public static final HttpStatus UNPROCESSABLE_ENTITY = new HttpStatus(422, true, "422 Unprocessable Entity");

    /**
     * Status {@code 423 Locked}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a>
     */
    public static final HttpStatus LOCKED = new HttpStatus(423, true, "423 Locked");
    /**
     * Status {@code 424 Failed Dependency}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a>
     */
    public static final HttpStatus FAILED_DEPENDENCY = new HttpStatus(424, true, "424 Failed Dependency");

    /**
     * Status {@code 426 Upgrade Required}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.15">RFC 7231, Section 6.5.15</a>
     */
    public static final HttpStatus UPGRADE_REQUIRED = new HttpStatus(426, true, "426 Upgrade Required");

    /**
     * Status {@code 428 Precondition Required}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6585">RFC 6585</a>
     */
    public static final HttpStatus PRECONDITION_REQUIRED = new HttpStatus(428, true, "428 Precondition Required");

    /**
     * Status {@code 429 Too Many Requests}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6585">RFC 6585</a>
     */
    public static final HttpStatus TOO_MANY_REQUESTS = new HttpStatus(429, true, "429 Too Many Requests");

    /**
     * Status {@code 431 Request Header Fields Too Large}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6585">RFC 6585</a>
     */
    public static final HttpStatus REQUEST_HEADER_FIELDS_TOO_LARGE = new HttpStatus(431, true, "431 Request Header " +
        "Fields Too Large");

    /**
     * Status {@code 451 Unavailable For Legal Reasons}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc7725">RFC 7725</a>
     */
    public static final HttpStatus UNAVAILABLE_FOR_LEGAL_REASONS = new HttpStatus(451, true, "451 Unavailable For " +
        "Legal Reasons");

    /**
     * Status {@code 500 Internal Server Error}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">RFC 7231, Section 6.6.1</a>
     */
    public static final HttpStatus INTERNAL_SERVER_ERROR = new HttpStatus(500, true, "500 Internal Server Error");

    /**
     * Status {@code 501 Not Implemented}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">RFC 7231, Section 6.6.2</a>
     */
    public static final HttpStatus NOT_IMPLEMENTED = new HttpStatus(501, true, "501 Not Implemented");

    /**
     * Status {@code 502 Bad Gateway}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">RFC 7231, Section 6.6.3</a>
     */
    public static final HttpStatus BAD_GATEWAY = new HttpStatus(502, true, "502 Bad Gateway");

    /**
     * Status {@code 503 Service Unavailable}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">RFC 7231, Section 6.6.4</a>
     */
    public static final HttpStatus SERVICE_UNAVAILABLE = new HttpStatus(503, true, "503 Service Unavailable");

    /**
     * Status {@code 504 Gateway Timeout}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">RFC 7231, Section 6.6.5</a>
     */
    public static final HttpStatus GATEWAY_TIMEOUT = new HttpStatus(504, true, "504 Gateway Timeout");

    /**
     * Status {@code 505 HTTP Version Not Supported}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">RFC 7231, Section 6.6.6</a>
     */
    public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = new HttpStatus(505, true, "505 HTTP Version Not " +
        "Supported");

    /**
     * Status {@code 506 Variant Also Negotiates}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2295">RFC 2295</a>
     */
    public static final HttpStatus VARIANT_ALSO_NEGOTIATES = new HttpStatus(506, true, "506 Variant Also Negotiates");

    /**
     * Status {@code 507 Insufficient Storage}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a>
     */
    public static final HttpStatus INSUFFICIENT_STORAGE = new HttpStatus(507, true, "507 Insufficient Storage");

    /**
     * Status {@code 508 Loop Detected}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc5842">RFC 5842</a>
     */
    public static final HttpStatus LOOP_DETECTED = new HttpStatus(508, true, "508 Loop Detected");

    /**
     * Status {@code 510 Not Extended}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2774">RFC 2774</a>
     */
    public static final HttpStatus NOT_EXTENDED = new HttpStatus(510, true, "510 Not Extended");

    /**
     * Status {@code 511 Network Authentication Required}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6585">RFC 6585</a>
     */
    public static final HttpStatus NETWORK_AUTHENTICATION_REQUIRED = new HttpStatus(511, true, "511 Network " +
        "Authentication Required");

    /**
     * Resolves {@link HttpStatus} from given status code.
     * <p>
     * If given {@code code} is a standardized status, a cached
     * {@link HttpStatus} is returned. Otherwise, a new instance is returned.
     *
     * @param code Code to resolve.
     * @return Resolved or new {@link HttpStatus}.
     */
    public static HttpStatus valueOf(final int code) {
        switch (code) {
            case 100: return CONTINUE;
            case 101: return SWITCHING_PROTOCOLS;
            case 102: return PROCESSING;
            case 200: return OK;
            case 201: return CREATED;
            case 202: return ACCEPTED;
            case 203: return NON_AUTHORITATIVE_INFORMATION;
            case 204: return NO_CONTENT;
            case 205: return RESET_CONTENT;
            case 206: return PARTIAL_CONTENT;
            case 207: return MULTI_STATUS;
            case 208: return ALREADY_REPORTED;
            case 226: return IM_USED;
            case 300: return MULTIPLE_CHOICES;
            case 301: return MOVED_PERMANENTLY;
            case 302: return FOUND;
            case 303: return SEE_OTHER;
            case 304: return NOT_MODIFIED;
            case 305: return USE_PROXY;
            case 307: return TEMPORARY_REDIRECT;
            case 308: return PERMANENT_REDIRECT;
            case 400: return BAD_REQUEST;
            case 401: return UNAUTHORIZED;
            case 402: return PAYMENT_REQUIRED;
            case 403: return FORBIDDEN;
            case 404: return NOT_FOUND;
            case 405: return METHOD_NOT_ALLOWED;
            case 406: return NOT_ACCEPTABLE;
            case 407: return PROXY_AUTHENTICATION_REQUIRED;
            case 408: return REQUEST_TIMEOUT;
            case 409: return CONFLICT;
            case 410: return GONE;
            case 411: return LENGTH_REQUIRED;
            case 412: return PRECONDITION_FAILED;
            case 413: return PAYLOAD_TOO_LARGE;
            case 414: return URI_TOO_LONG;
            case 415: return UNSUPPORTED_MEDIA_TYPE;
            case 416: return RANGE_NOT_SATISFIABLE;
            case 417: return EXPECTATION_FAILED;
            case 418: return IM_A_TEAPOT;
            case 421: return MISDIRECTED_REQUEST;
            case 422: return UNPROCESSABLE_ENTITY;
            case 423: return LOCKED;
            case 424: return FAILED_DEPENDENCY;
            case 426: return UPGRADE_REQUIRED;
            case 428: return PRECONDITION_REQUIRED;
            case 429: return TOO_MANY_REQUESTS;
            case 431: return REQUEST_HEADER_FIELDS_TOO_LARGE;
            case 451: return UNAVAILABLE_FOR_LEGAL_REASONS;
            case 500: return INTERNAL_SERVER_ERROR;
            case 501: return NOT_IMPLEMENTED;
            case 502: return BAD_GATEWAY;
            case 503: return SERVICE_UNAVAILABLE;
            case 504: return GATEWAY_TIMEOUT;
            case 505: return HTTP_VERSION_NOT_SUPPORTED;
            case 506: return VARIANT_ALSO_NEGOTIATES;
            case 507: return INSUFFICIENT_STORAGE;
            case 508: return LOOP_DETECTED;
            case 510: return NOT_EXTENDED;
            case 511: return NETWORK_AUTHENTICATION_REQUIRED;
        }
        return new HttpStatus(code, false, "" + code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        HttpStatus that = (HttpStatus) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return text;
    }
}