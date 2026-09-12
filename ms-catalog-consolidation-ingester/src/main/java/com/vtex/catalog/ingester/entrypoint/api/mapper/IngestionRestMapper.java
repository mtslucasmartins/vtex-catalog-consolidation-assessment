package com.vtex.catalog.ingester.entrypoint.api.mapper;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionSnapshot;
import com.vtex.catalog.ingester.application.usecase.IngestCommand;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionIn;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionOut;
import org.springframework.stereotype.Component;

@Component
public class IngestionRestMapper {

    public IngestCommand toCommand(IngestionIn in) {
        return IngestCommand.builder()
                .fileName(in.getFilename())
                .contentLength(in.getContentLength())
                .content(in.getContent())
                .build();
    }

    public IngestionOut toOut(IngestionSnapshot snapshot) {
        return IngestionOut.builder()
                .id(snapshot.getId().getValue())
                .fileName(snapshot.getFileName())
                .status(snapshot.getStatus().name())
                .failureReason(snapshot.getFailureReason())
                .createdAt(snapshot.getCreatedAt())
                .updatedAt(snapshot.getUpdatedAt())
                .build();
    }
}
