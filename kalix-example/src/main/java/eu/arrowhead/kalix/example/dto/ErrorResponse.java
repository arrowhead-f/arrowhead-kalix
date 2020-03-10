package eu.arrowhead.kalix.example.dto;

import eu.arrowhead.kalix.dto.Readable;

@Readable
public interface ErrorResponse {
    String errorMessage();
    int errorCode();
    String exceptionType();
}
