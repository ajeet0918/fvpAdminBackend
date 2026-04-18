package com.agriplatform.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InquiryDocumentStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private final Path inquiryUploadDir;

    public InquiryDocumentStorageService(@Value("${app.upload.base-dir:uploads}") String baseUploadDir) {
        this.inquiryUploadDir = Paths.get(baseUploadDir, "inquiries").toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, String bucket) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Document size must be less than or equal to 10 MB");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Unsupported document file type");
        }

        String safeBucket = sanitizeBucket(bucket);
        Path targetDir = inquiryUploadDir.resolve(safeBucket).normalize();

        try {
            Files.createDirectories(targetDir);
            String extension = resolveExtension(file.getOriginalFilename(), contentType);
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = targetDir.resolve(fileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/inquiries/" + safeBucket + "/" + fileName;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to store inquiry document");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType.isBlank()) {
            return false;
        }
        return contentType.startsWith("image/")
                || contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private String sanitizeBucket(String value) {
        if (value == null || value.isBlank()) {
            return "general";
        }
        String sanitized = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return sanitized.isBlank() ? "general" : sanitized;
    }

    private String resolveExtension(String originalName, String contentType) {
        if (originalName != null) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                String ext = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (ext.matches("\\.[a-z0-9]{2,8}")) {
                    return ext;
                }
            }
        }
        if (contentType.contains("png")) return ".png";
        if (contentType.contains("webp")) return ".webp";
        if (contentType.contains("gif")) return ".gif";
        if (contentType.contains("pdf")) return ".pdf";
        if (contentType.contains("wordprocessingml")) return ".docx";
        if (contentType.contains("msword")) return ".doc";
        return ".bin";
    }
}
