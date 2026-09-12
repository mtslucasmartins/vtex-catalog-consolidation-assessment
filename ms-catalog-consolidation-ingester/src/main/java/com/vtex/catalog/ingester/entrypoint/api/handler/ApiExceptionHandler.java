package com.vtex.catalog.ingester.entrypoint.api.handler;

import com.vtex.catalog.ingester.application.common.exception.IngestionNotFoundException;
import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.common.exception.DomainException;
import com.vtex.catalog.ingester.entrypoint.api.dto.ErrorOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({DomainException.class, InvalidIngestionFileException.class, IllegalArgumentException.class,
            MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ErrorOut> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorOut> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Expected multipart/form-data with a 'file' part");
    }

    @ExceptionHandler(IngestionNotFoundException.class)
    public ResponseEntity<ErrorOut> handleNotFound(IngestionNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorOut> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private ResponseEntity<ErrorOut> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorOut.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build());
    }
}
