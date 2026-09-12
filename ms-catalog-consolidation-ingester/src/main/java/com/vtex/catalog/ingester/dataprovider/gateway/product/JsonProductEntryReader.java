package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.ingester.application.gateway.storage.FileStorageGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductEntryReaderGateway;
import com.vtex.catalog.ingester.dataprovider.file.ProductEntryIn;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

/** Streams a JSON array with Jackson's incremental parser so file size does not bound memory. */
@Component
@RequiredArgsConstructor
public class JsonProductEntryReader implements ProductEntryReaderGateway {

    private final ObjectMapper objectMapper;

    private final FileStorageGateway fileStorageGateway;

    @Override
    public long read(String filePath, BiConsumer<Integer, ProductEntryPayload> entryConsumer) {
        try (InputStream in = fileStorageGateway.open(filePath);
             JsonParser parser = objectMapper.getFactory().createParser(in)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new InvalidIngestionFileException("File must be a JSON array of products");
            }

            long total = 0;
            int entryIndex = 0;
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                entryConsumer.accept(entryIndex++, objectMapper.readValue(parser, ProductEntryIn.class).toDomain());
                total++;
            }
            return total;
        } catch (JsonProcessingException e) {
            throw new InvalidIngestionFileException("File is not valid JSON: " + e.getOriginalMessage(), e);
        } catch (IOException e) {
            throw new InvalidIngestionFileException("Could not read file: " + e.getMessage(), e);
        }
    }
}
