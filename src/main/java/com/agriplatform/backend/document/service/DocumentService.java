package com.agriplatform.backend.document.service;

import com.agriplatform.backend.*;
import com.agriplatform.backend.auth.controller.*;
import com.agriplatform.backend.auth.dto.*;
import com.agriplatform.backend.auth.service.*;
import com.agriplatform.backend.category.controller.*;
import com.agriplatform.backend.category.model.*;
import com.agriplatform.backend.category.repository.*;
import com.agriplatform.backend.common.controller.*;
import com.agriplatform.backend.config.*;
import com.agriplatform.backend.customer.controller.*;
import com.agriplatform.backend.customer.dto.*;
import com.agriplatform.backend.customer.model.*;
import com.agriplatform.backend.customer.repository.*;
import com.agriplatform.backend.customer.service.*;
import com.agriplatform.backend.document.controller.*;
import com.agriplatform.backend.document.dto.*;
import com.agriplatform.backend.document.model.*;
import com.agriplatform.backend.document.repository.*;
import com.agriplatform.backend.document.service.*;
import com.agriplatform.backend.inquiry.controller.*;
import com.agriplatform.backend.inquiry.dto.*;
import com.agriplatform.backend.inquiry.model.*;
import com.agriplatform.backend.inquiry.repository.*;
import com.agriplatform.backend.inquiry.service.*;
import com.agriplatform.backend.investor.controller.*;
import com.agriplatform.backend.investor.dto.*;
import com.agriplatform.backend.investor.model.*;
import com.agriplatform.backend.investor.repository.*;
import com.agriplatform.backend.investor.service.*;
import com.agriplatform.backend.lead.controller.*;
import com.agriplatform.backend.lead.dto.*;
import com.agriplatform.backend.lead.model.*;
import com.agriplatform.backend.lead.repository.*;
import com.agriplatform.backend.lead.service.*;
import com.agriplatform.backend.order.controller.*;
import com.agriplatform.backend.order.dto.*;
import com.agriplatform.backend.order.model.*;
import com.agriplatform.backend.order.repository.*;
import com.agriplatform.backend.order.service.*;
import com.agriplatform.backend.portal.controller.*;
import com.agriplatform.backend.portal.dto.*;
import com.agriplatform.backend.portal.model.*;
import com.agriplatform.backend.portal.repository.*;
import com.agriplatform.backend.portal.service.*;
import com.agriplatform.backend.product.controller.*;
import com.agriplatform.backend.product.dto.*;
import com.agriplatform.backend.product.model.*;
import com.agriplatform.backend.product.repository.*;
import com.agriplatform.backend.product.service.*;
import com.agriplatform.backend.security.*;
import com.agriplatform.backend.user.controller.*;
import com.agriplatform.backend.user.dto.*;
import com.agriplatform.backend.user.model.*;
import com.agriplatform.backend.user.repository.*;
import com.agriplatform.backend.user.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final Path uploadBaseDir;
    private final AppDocumentRepository appDocumentRepository;

    public DocumentService(
            @Value("${app.upload.base-dir:uploads}") String baseUploadDir,
            AppDocumentRepository appDocumentRepository
    ) {
        this.uploadBaseDir = Paths.get(baseUploadDir).toAbsolutePath().normalize();
        this.appDocumentRepository = appDocumentRepository;
    }

    @Transactional
    public AppDocument upload(MultipartFile file, UploadRequest request) {
        validate(file, request);

        String safeFolder = sanitizeFolder(request.folder());
        String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String objectKey = safeFolder + "/" + storedFileName;
        Path targetPath = resolveTargetPath(objectKey);

        try {
            Files.createDirectories(targetPath.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String checksum = toHex(digest.digest());
            String publicPath = "/uploads/" + objectKey.replace('\\', '/');

            AppDocument document = new AppDocument(
                    normalizeFileName(file.getOriginalFilename()),
                    objectKey.replace('\\', '/'),
                    publicPath,
                    normalizeNullable(file.getContentType()),
                    file.getSize(),
                    checksum,
                    request.module().trim().toUpperCase(Locale.ROOT),
                    request.ownerId()
            );
            return appDocumentRepository.save(document);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException("Unable to store document");
        }
    }

    @Transactional(readOnly = true)
    public AppDocument getById(UUID id) {
        return appDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    @Transactional(readOnly = true)
    public Resource download(UUID id) {
        AppDocument document = getById(id);
        if (document.getStatus() == DocumentStatus.DELETED) {
            throw new IllegalArgumentException("Document is deleted");
        }

        Path targetPath = resolveTargetPath(document.getObjectKey());
        if (!Files.exists(targetPath)) {
            throw new IllegalArgumentException("Document file not found");
        }
        return new FileSystemResource(targetPath);
    }

    @Transactional
    public AppDocument updateMetadata(UUID id, String originalFileName, String module, Long ownerId) {
        AppDocument document = getById(id);
        document.updateMetadata(
                normalizeNullable(originalFileName),
                normalizeNullable(module),
                ownerId
        );
        return appDocumentRepository.save(document);
    }

    @Transactional
    public void delete(UUID id, boolean deletePhysicalFile) {
        AppDocument document = getById(id);
        if (deletePhysicalFile) {
            Path targetPath = resolveTargetPath(document.getObjectKey());
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to delete document file");
            }
        }
        document.markDeleted();
        appDocumentRepository.save(document);
    }

    private void validate(MultipartFile file, UploadRequest request) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Upload request is required");
        }
        if (request.module() == null || request.module().isBlank()) {
            throw new IllegalArgumentException("Document module is required");
        }
        if (request.folder() == null || request.folder().isBlank()) {
            throw new IllegalArgumentException("Document folder is required");
        }
        if (request.maxFileSizeBytes() > 0 && file.getSize() > request.maxFileSizeBytes()) {
            throw new IllegalArgumentException("Document size exceeds allowed limit");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        boolean matchesImage = request.allowImageMimePrefix() && contentType.startsWith("image/");
        boolean matchesExact = request.allowedContentTypes().contains(contentType);
        if (!matchesImage && !matchesExact) {
            throw new IllegalArgumentException("Unsupported document file type");
        }
    }

    private Path resolveTargetPath(String objectKey) {
        Path targetPath = uploadBaseDir.resolve(objectKey).normalize();
        if (!targetPath.startsWith(uploadBaseDir)) {
            throw new IllegalArgumentException("Invalid document path");
        }
        return targetPath;
    }

    private String sanitizeFolder(String folder) {
        String[] parts = folder.split("[/\\\\]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            String clean = part.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9-]", "-")
                    .replaceAll("-{2,}", "-")
                    .replaceAll("^-|-$", "");
            if (clean.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(clean);
        }

        if (builder.length() == 0) {
            throw new IllegalArgumentException("Invalid folder name");
        }
        return builder.toString();
    }

    private String resolveExtension(String originalFileName, String contentType) {
        if (originalFileName != null) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFileName.length() - 1) {
                String ext = originalFileName.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (ext.matches("\\.[a-z0-9]{2,8}")) {
                    return ext;
                }
            }
        }

        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (type.contains("png")) return ".png";
        if (type.contains("webp")) return ".webp";
        if (type.contains("gif")) return ".gif";
        if (type.contains("pdf")) return ".pdf";
        if (type.contains("wordprocessingml")) return ".docx";
        if (type.contains("msword")) return ".doc";
        if (type.contains("jpeg") || type.contains("jpg")) return ".jpg";
        return ".bin";
    }

    private String normalizeFileName(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "document" : normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte current : bytes) {
            builder.append(Character.forDigit((current >> 4) & 0xF, 16));
            builder.append(Character.forDigit(current & 0xF, 16));
        }
        return builder.toString();
    }

    public record UploadRequest(
            String folder,
            String module,
            Long ownerId,
            long maxFileSizeBytes,
            boolean allowImageMimePrefix,
            Set<String> allowedContentTypes
    ) {
        public UploadRequest {
            if (allowedContentTypes == null) {
                allowedContentTypes = Set.of();
            }
        }
    }
}

