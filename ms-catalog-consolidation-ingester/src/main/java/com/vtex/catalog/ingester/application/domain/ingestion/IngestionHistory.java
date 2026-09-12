package com.vtex.catalog.ingester.application.domain.ingestion;

import com.vtex.catalog.ingester.application.common.stereotypes.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class IngestionHistory implements DomainEntity {

    private final IngestionId id;

    private final String fileName;

    private IngestionChronology chronology;

    public static IngestionHistory record(IngestionId id, String fileName) {
        return IngestionHistory.builder()
                .id(id)
                .fileName(fileName)
                .chronology(IngestionChronology.now())
                .build();
    }

    public Instant getCreatedAt() {
        return chronology.getCreatedAt();
    }

    public Instant getUpdatedAt() {
        return chronology.getUpdatedAt();
    }

    public void touch() {
        this.chronology.touch();
    }
}
