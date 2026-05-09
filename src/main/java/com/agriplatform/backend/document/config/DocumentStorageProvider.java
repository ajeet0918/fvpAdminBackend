package com.agriplatform.backend.document.config;

public enum DocumentStorageProvider {
    LOCAL,
    S3;

    public static DocumentStorageProvider from(String value) {
        if (value == null || value.isBlank()) {
            return LOCAL;
        }
        return DocumentStorageProvider.valueOf(value.trim().toUpperCase());
    }
}
