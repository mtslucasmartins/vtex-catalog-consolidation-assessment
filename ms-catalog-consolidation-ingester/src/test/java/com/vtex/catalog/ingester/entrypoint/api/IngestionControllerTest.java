package com.vtex.catalog.ingester.entrypoint.api;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionSnapshot;
import com.vtex.catalog.ingester.application.usecase.GetIngestionByIdQuery;
import com.vtex.catalog.ingester.application.usecase.GetIngestionByIdUseCase;
import com.vtex.catalog.ingester.application.usecase.IngestFileUseCase;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionIn;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionOut;
import com.vtex.catalog.ingester.entrypoint.api.mapper.IngestionRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionControllerTest {

    @Mock
    private IngestFileUseCase ingestFileUseCase;

    @Mock
    private GetIngestionByIdUseCase getIngestionByIdUseCase;

    private IngestionController controller;

    @BeforeEach
    void setUp() {
        controller = new IngestionController(ingestFileUseCase, getIngestionByIdUseCase, new IngestionRestMapper());
    }

    @Test
    void givenMultipartFile_whenUpload_thenReturnsAcceptedSnapshot() throws Exception {
        // Given
        var file = new MockMultipartFile("file", "products.json", "application/json", "[]".getBytes());
        var snapshot = IngestionSnapshot.builder()
                .id(IngestionId.of("ing-1"))
                .fileName("products.json")
                .status(IngestionDispatchStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(ingestFileUseCase.execute(any())).thenReturn(snapshot);

        // When
        var response = controller.upload(file);

        // Then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("ing-1", response.getBody().getId());
    }

    @Test
    void givenExistingIngestion_whenGetById_thenReturnsOkSnapshot() {
        // Given
        var snapshot = IngestionSnapshot.builder()
                .id(IngestionId.of("ing-1"))
                .fileName("products.json")
                .status(IngestionDispatchStatus.DISPATCHED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(getIngestionByIdUseCase.execute(any(GetIngestionByIdQuery.class))).thenReturn(snapshot);

        // When
        var response = controller.getById("ing-1");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("DISPATCHED", response.getBody().getStatus());
    }
}
