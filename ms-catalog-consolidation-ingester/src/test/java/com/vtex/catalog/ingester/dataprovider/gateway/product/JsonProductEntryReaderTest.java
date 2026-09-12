package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.ingester.application.gateway.storage.FileStorageGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonProductEntryReaderTest {

    @Mock
    private FileStorageGateway fileStorageGateway;

    private JsonProductEntryReader reader;

    @BeforeEach
    void setUp() {
        reader = new JsonProductEntryReader(new ObjectMapper(), fileStorageGateway);
    }

    @Test
    void givenJsonArray_whenRead_thenStreamsEachProduct() throws IOException {
        // Given
        var json = """
                [
                  {
                    "Id": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
                    "SellerName": "MegaStore",
                    "Name": "Smartphone Galaxy S23",
                    "Brand": "Samsung",
                    "Category": "Phones"
                  }
                ]
                """;
        when(fileStorageGateway.open("ing-1")).thenReturn(new ByteArrayInputStream(json.getBytes()));
        var products = new ArrayList<ProductEntryPayload>();

        // When
        var total = reader.read("ing-1", (index, product) -> products.add(product));

        // Then
        assertEquals(1L, total);
        assertEquals("MegaStore", products.getFirst().getSellerName());
        assertEquals("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d", products.getFirst().getSellerProductId());
    }

    @Test
    void givenNonArrayJson_whenRead_thenThrowsInvalidIngestionFileException() throws IOException {
        // Given
        when(fileStorageGateway.open("ing-1")).thenReturn(new ByteArrayInputStream("{}".getBytes()));

        // When / Then
        assertThrows(InvalidIngestionFileException.class,
                () -> reader.read("ing-1", (index, product) -> {
                }));
    }

    @Test
    void givenMalformedJson_whenRead_thenThrowsInvalidIngestionFileException() throws IOException {
        // Given
        when(fileStorageGateway.open("ing-1")).thenReturn(new ByteArrayInputStream("{not-json".getBytes()));

        // When / Then
        assertThrows(InvalidIngestionFileException.class,
                () -> reader.read("ing-1", (index, product) -> {
                }));
    }

    @Test
    void givenIoFailure_whenRead_thenThrowsInvalidIngestionFileException() throws IOException {
        // Given
        when(fileStorageGateway.open("ing-1")).thenReturn(new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("s3 down");
            }
        });

        // When / Then
        assertThrows(InvalidIngestionFileException.class,
                () -> reader.read("ing-1", (index, product) -> {
                }));
    }
}
