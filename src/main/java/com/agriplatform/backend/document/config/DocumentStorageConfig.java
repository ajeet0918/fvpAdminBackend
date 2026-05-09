package com.agriplatform.backend.document.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class DocumentStorageConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.document", name = "storage-provider", havingValue = "S3")
    public S3Client s3Client(DocumentStorageProperties properties) {
        S3ClientBuilder builder = S3Client.builder();
        String region = properties.getS3().getRegion();
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region.trim()));
        }
        return builder.build();
    }
}
