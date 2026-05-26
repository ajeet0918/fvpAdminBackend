package com.agriplatform.backend;

import com.agriplatform.backend.document.config.DocumentStorageProperties;
import com.agriplatform.backend.payment.config.CashfreeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CashfreeProperties.class, DocumentStorageProperties.class})
public class BackendApplication {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
