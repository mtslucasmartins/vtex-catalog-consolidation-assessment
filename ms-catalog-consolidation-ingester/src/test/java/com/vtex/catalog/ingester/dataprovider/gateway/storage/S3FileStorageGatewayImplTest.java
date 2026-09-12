package com.vtex.catalog.ingester.dataprovider.gateway.storage;

import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileStorageGatewayImplTest {

    @Mock
    private S3Client s3Client;

    private S3FileStorageGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        var properties = new IngesterProperties();
        properties.getStorage().setBucket("catalog-ingestions");
        gateway = new S3FileStorageGatewayImpl(s3Client, properties);
    }

    @Test
    void givenFileContent_whenStore_thenUploadsToS3AndReturnsKey() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var content = new ByteArrayInputStream("[]".getBytes());

        // When
        var key = gateway.store(ingestionId, "products.json", 2L, content);

        // Then
        assertEquals("ing-1", key);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void givenS3Failure_whenStore_thenThrowsInvalidIngestionFileException() {
        // Given
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.builder().message("upload failed").build());

        // When / Then
        assertThrows(InvalidIngestionFileException.class, () -> gateway.store(
                IngestionId.of("ing-1"),
                "products.json",
                2L,
                new ByteArrayInputStream("[]".getBytes())));
    }

    @Test
    void givenObjectKey_whenOpen_thenReturnsS3ObjectStream() {
        // Given
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> responseStream = org.mockito.Mockito.mock(ResponseInputStream.class);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

        // When
        var opened = gateway.open("ing-1");

        // Then
        assertEquals(responseStream, opened);
    }

    @Test
    void givenS3ReadFailure_whenOpen_thenThrowsInvalidIngestionFileException() {
        // Given
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(SdkClientException.builder().message("read failed").build());

        // When / Then
        assertThrows(InvalidIngestionFileException.class, () -> gateway.open("ing-1"));
    }
}
