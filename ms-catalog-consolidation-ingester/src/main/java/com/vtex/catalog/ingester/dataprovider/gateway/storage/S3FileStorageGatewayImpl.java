package com.vtex.catalog.ingester.dataprovider.gateway.storage;

import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.storage.FileStorageGateway;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class S3FileStorageGatewayImpl implements FileStorageGateway {

    private final S3Client s3Client;
    private final IngesterProperties properties;

    @Override
    public String store(IngestionId ingestionId, String fileName, long contentLength, InputStream content) {
        var key = ingestionId.getValue();
        try {
            var request = PutObjectRequest.builder()
                    .bucket(properties.getStorage().getBucket())
                    .key(key)
                    .contentType("application/json")
                    .contentDisposition(ContentDisposition.attachment()
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
                            .toString())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));
            return key;
        } catch (SdkException e) {
            throw new InvalidIngestionFileException("Could not store file '" + fileName + "'", e);
        }
    }

    @Override
    public InputStream open(String objectKey) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(properties.getStorage().getBucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException e) {
            throw new InvalidIngestionFileException("Could not read file: " + e.getMessage(), e);
        }
    }
}
