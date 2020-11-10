package se.arkalix.net.http.service;

import se.arkalix.net.MessageOutgoingWithImplicitCodec;
import se.arkalix.net.http.HttpOutgoingResponse;

/**
 * An outgoing HTTP response, to be sent by an {@link HttpService}.
 */
public interface HttpServiceResponse
    extends HttpOutgoingResponse<HttpServiceResponse>, MessageOutgoingWithImplicitCodec<HttpServiceResponse>
{}
