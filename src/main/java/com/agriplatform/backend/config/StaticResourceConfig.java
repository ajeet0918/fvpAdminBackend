package com.agriplatform.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String baseUploadDir;

    public StaticResourceConfig(@Value("${app.upload.base-dir:uploads}") String baseUploadDir) {
        this.baseUploadDir = baseUploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absoluteBaseDir = Paths.get(baseUploadDir).toAbsolutePath().normalize();
        String location = absoluteBaseDir.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
