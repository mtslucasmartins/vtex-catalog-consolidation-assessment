package com.vtex.catalog.ingester.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    S3Client s3Client(IngesterProperties properties) {
        var storage = properties.getStorage();
        var builder = S3Client.builder().region(Region.of(storage.getRegion()));
        if (StringUtils.hasText(storage.getEndpoint())) {
            builder.endpointOverride(URI.create(storage.getEndpoint()))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        return builder.build();
    }
}
