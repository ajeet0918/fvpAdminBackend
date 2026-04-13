package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.ImageUploadResponse;
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
public class ProductImageStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private final Path productUploadDir;

    public ProductImageStorageService(@Value("${app.upload.base-dir:uploads}") String baseUploadDir) {
        this.productUploadDir = Paths.get(baseUploadDir, "products").toAbsolutePath().normalize();
    }

    public ImageUploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image size must be less than or equal to 5 MB");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        try {
            Files.createDirectories(productUploadDir);
            String extension = resolveExtension(file.getOriginalFilename(), contentType);
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = productUploadDir.resolve(fileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String imageUrl = "/uploads/products/" + fileName;
            return new ImageUploadResponse(imageUrl, fileName);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to store image file");
        }
    }

    private String resolveExtension(String originalName, String contentType) {
        if (originalName != null) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                String ext = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (ext.matches("\\.[a-z0-9]{2,6}")) {
                    return ext;
                }
            }
        }

        if (contentType.contains("png")) return ".png";
        if (contentType.contains("webp")) return ".webp";
        if (contentType.contains("gif")) return ".gif";
        return ".jpg";
    }
}
