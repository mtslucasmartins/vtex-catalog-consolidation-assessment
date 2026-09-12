package com.vtex.catalog.ingester.entrypoint.api.handler;

import com.vtex.catalog.ingester.application.common.exception.IngestionNotFoundException;
import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void givenIngestionNotFound_whenHandleNotFound_thenReturns404() {
        // Given
        var exception = new IngestionNotFoundException(IngestionId.of("ing-1"));

        // When
        var response = handler.handleNotFound(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void givenInvalidFile_whenHandleBadRequest_thenReturns400() {
        // Given
        var exception = new InvalidIngestionFileException("bad json");

        // When
        var response = handler.handleBadRequest(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad json", response.getBody().getMessage());
    }

    @Test
    void givenUnsupportedMediaType_whenHandleUnsupportedMediaType_thenReturns415() {
        // Given
        var exception = new HttpMediaTypeNotSupportedException("text/plain");

        // When
        var response = handler.handleUnsupportedMediaType(exception);

        // Then
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    void givenUnexpectedError_whenHandleUnexpected_thenReturns500() {
        // Given
        var exception = new RuntimeException("boom");

        // When
        var response = handler.handleUnexpected(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }
}
