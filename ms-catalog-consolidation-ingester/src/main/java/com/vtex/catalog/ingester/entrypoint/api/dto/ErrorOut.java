package com.vtex.catalog.ingester.entrypoint.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ErrorOut {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
}
