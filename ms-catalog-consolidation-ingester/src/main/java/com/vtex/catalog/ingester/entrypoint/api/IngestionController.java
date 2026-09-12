package com.vtex.catalog.ingester.entrypoint.api;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.usecase.GetIngestionByIdQuery;
import com.vtex.catalog.ingester.application.usecase.GetIngestionByIdUseCase;
import com.vtex.catalog.ingester.application.usecase.IngestFileUseCase;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionIn;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionOut;
import com.vtex.catalog.ingester.entrypoint.api.mapper.IngestionRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/ingestions")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestFileUseCase ingestFileUseCase;

    private final GetIngestionByIdUseCase getIngestionByIdUseCase;

    private final IngestionRestMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestionOut> upload(@RequestPart("file") MultipartFile file) throws IOException {
        var snapshot = ingestFileUseCase.execute(mapper.toCommand(IngestionIn.from(file)));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapper.toOut(snapshot));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngestionOut> getById(@PathVariable String id) {
        var query = GetIngestionByIdQuery.from(IngestionId.of(id));
        return ResponseEntity.ok(mapper.toOut(getIngestionByIdUseCase.execute(query)));
    }
}
