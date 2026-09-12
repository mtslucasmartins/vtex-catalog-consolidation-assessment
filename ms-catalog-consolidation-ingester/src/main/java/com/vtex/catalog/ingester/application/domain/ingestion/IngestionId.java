package com.vtex.catalog.ingester.application.domain.ingestion;

import com.vtex.catalog.ingester.application.common.stereotypes.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public final class IngestionId extends ValueObject {

    private final String value;

    private IngestionId(String value) {
        assertNotEmpty(value, "Ingestion id must not be blank");
        this.value = value;
    }

    public static IngestionId generate() {
        return new IngestionId(UUID.randomUUID().toString());
    }

    public static IngestionId of(String value) {
        return new IngestionId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
