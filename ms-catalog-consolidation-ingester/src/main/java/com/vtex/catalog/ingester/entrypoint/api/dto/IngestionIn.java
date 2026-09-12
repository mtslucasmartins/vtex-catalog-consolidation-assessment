package com.vtex.catalog.ingester.entrypoint.api.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Getter
@Builder
public class IngestionIn {

    private static final String DEFAULT_FILENAME = "product_entries_unnamed.json";

    private String filename;

    private final long contentLength;

    private final InputStream content;

    public static IngestionIn from(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        var filename = Optional.ofNullable(file.getOriginalFilename()).orElse(DEFAULT_FILENAME);
        return IngestionIn.builder()
                .filename(filename)
                .contentLength(file.getSize())
                .content(file.getInputStream())
                .build();
    }
}
