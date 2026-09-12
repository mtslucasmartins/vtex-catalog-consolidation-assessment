package com.vtex.catalog.ingester.application.common.exception;

public class InvalidIngestionFileException extends RuntimeException {

    public InvalidIngestionFileException(String message) {
        super(message);
    }

    public InvalidIngestionFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
