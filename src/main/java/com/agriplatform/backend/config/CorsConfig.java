package com.agriplatform.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedOrigins = {
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5174",
                        "http://localhost:4173",
                        "http://127.0.0.1:4173",
                        "http://localhost:4174",
                        "http://127.0.0.1:4174"
                };

                registry.addMapping("/api/**")
                        .allowedOrigins(
                                allowedOrigins
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

                registry.addMapping("/uploads/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "OPTIONS");
            }
        };
    }
}
