package com.vtex.catalog.ingester.application.domain.ingestion;

import com.vtex.catalog.ingester.application.common.stereotypes.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class IngestionChronology extends ValueObject {

    private Instant createdAt;

    private Instant updatedAt;

    protected void touch() {
        this.updatedAt = Instant.now();
    }

    public static IngestionChronology now() {
        var now = Instant.now();
        return IngestionChronology.builder().createdAt(now).updatedAt(now).build();
    }
}
