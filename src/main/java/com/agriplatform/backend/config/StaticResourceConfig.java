package com.agriplatform.backend.config;

import com.agriplatform.backend.document.config.DocumentStorageProperties;
import com.agriplatform.backend.document.config.DocumentStorageProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final DocumentStorageProperties storageProperties;

    public StaticResourceConfig(DocumentStorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (storageProperties.resolvedProvider() != DocumentStorageProvider.LOCAL) {
            return;
        }

        Path absoluteBaseDir = Paths.get(storageProperties.getLocalBaseDir()).toAbsolutePath().normalize();
        String location = absoluteBaseDir.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
