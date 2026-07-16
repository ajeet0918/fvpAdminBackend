package com.agriplatform.backend.document.service;

import com.agriplatform.backend.document.config.DocumentStorageProperties;
import com.agriplatform.backend.document.config.DocumentStorageProvider;
import com.agriplatform.backend.document.model.AppDocument;
import com.agriplatform.backend.document.model.DocumentStatus;
import com.agriplatform.backend.document.repository.AppDocumentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class DocumentService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentService.class);
    public static final String PRODUCT_IMAGE_MODULE = "PRODUCT_IMAGE";

    private final DocumentStorageProperties storageProperties;
    private final DocumentStorageProvider storageProvider;
    private final Path uploadBaseDir;
    private final AppDocumentRepository appDocumentRepository;
    private final S3Client s3Client;

    public DocumentService(
            DocumentStorageProperties storageProperties,
            AppDocumentRepository appDocumentRepository,
            ObjectProvider<S3Client> s3ClientProvider
    ) {
        this.storageProperties = storageProperties;
        this.storageProvider = storageProperties.resolvedProvider();
        this.uploadBaseDir = Paths.get(storageProperties.getLocalBaseDir()).toAbsolutePath().normalize();
        this.appDocumentRepository = appDocumentRepository;
        this.s3Client = s3ClientProvider.getIfAvailable();

        if (storageProvider == DocumentStorageProvider.S3) {
            if (s3Client == null) {
                throw new IllegalArgumentException("S3 storage provider selected but S3 client is not configured");
            }
            if (isBlank(storageProperties.getS3().getBucket())) {
                throw new IllegalArgumentException("S3 storage provider selected but app.document.s3.bucket is missing");
            }
        }
    }

    @Transactional
    public AppDocument upload(MultipartFile file, UploadRequest request) {
        validate(file, request);

        String safeFolder = sanitizeFolder(request.folder());
        String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String objectKey = buildObjectKey(safeFolder, storedFileName);
        String contentType = normalizeNullable(file.getContentType());

        try {
            byte[] content = file.getBytes();
            String checksum = sha256Hex(content);

            if (storageProvider == DocumentStorageProvider.S3) {
                storeS3(objectKey, content, contentType);
            } else {
                storeLocal(objectKey, content);
            }

            String normalizedModule = request.module().trim().toUpperCase(Locale.ROOT);
            AppDocument document = new AppDocument(
                    normalizeFileName(file.getOriginalFilename()),
                    objectKey,
                    buildPendingPath(objectKey),
                    contentType,
                    file.getSize(),
                    checksum,
                    normalizedModule,
                    request.ownerId()
            );
            document.updateStorageProvider(storageProvider.name());
            AppDocument saved = appDocumentRepository.save(document);
            saved.updatePath(buildAccessPath(saved));
            return appDocumentRepository.save(saved);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read document file");
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

        if (storageProvider == DocumentStorageProvider.S3) {
            return downloadFromS3(document.getObjectKey());
        }

        Path targetPath = resolveTargetPath(document.getObjectKey());
        if (!Files.exists(targetPath)) {
            throw new IllegalArgumentException("Document file not found");
        }
        return new FileSystemResource(targetPath);
    }

    public boolean isPublicDocument(AppDocument document) {
        return document != null
                && document.getStatus() == DocumentStatus.ACTIVE
                && PRODUCT_IMAGE_MODULE.equalsIgnoreCase(normalizeNullable(document.getModule()));
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
            if (storageProvider == DocumentStorageProvider.S3) {
                deleteFromS3(document.getObjectKey());
            } else {
                Path targetPath = resolveTargetPath(document.getObjectKey());
                try {
                    Files.deleteIfExists(targetPath);
                } catch (IOException ex) {
                    throw new IllegalArgumentException("Unable to delete document file");
                }
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

        String contentType = Objects.toString(file.getContentType(), "").toLowerCase(Locale.ROOT);
        boolean matchesImage = request.allowImageMimePrefix() && contentType.startsWith("image/");
        Set<String> allowedContentTypes = request.allowedContentTypes() == null
                ? Set.of()
                : request.allowedContentTypes();
        boolean matchesExact = allowedContentTypes.contains(contentType);
        if (!matchesImage && !matchesExact) {
            throw new IllegalArgumentException("Unsupported document file type");
        }
    }

    private void storeLocal(String objectKey, byte[] content) {
        Path targetPath = resolveTargetPath(objectKey);
        Path parentDirectory = targetPath.getParent();
        if (parentDirectory == null) {
            throw new IllegalArgumentException("Invalid document path");
        }
        try {
            Files.createDirectories(parentDirectory);
            Files.write(targetPath, content);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to store document");
        }
    }

    private void storeS3(String objectKey, byte[] content, String contentType) {
        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket().trim())
                    .key(objectKey);
            if (!isBlank(contentType)) {
                requestBuilder.contentType(contentType.trim());
            }
            s3Client.putObject(requestBuilder.build(), RequestBody.fromBytes(content));
        } catch (S3Exception ex) {
            throw new IllegalArgumentException("Unable to store document in S3");
        }
    }

    private Resource downloadFromS3(String objectKey) {
        try {
            byte[] bytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket().trim())
                            .key(objectKey)
                            .build()
            ).asByteArray();
            return new ByteArrayResource(bytes);
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException("Document file not found");
        } catch (S3Exception ex) {
            throw new IllegalArgumentException("Unable to read document file");
        }
    }

    private void deleteFromS3(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket().trim())
                    .key(objectKey)
                    .build());
        } catch (S3Exception ex) {
            throw new IllegalArgumentException("Unable to delete document file");
        }
    }

    private String buildObjectKey(String safeFolder, String storedFileName) {
        String relative = safeFolder + "/" + storedFileName;
        if (storageProvider != DocumentStorageProvider.S3) {
            return relative;
        }

        String keyPrefix = normalizeS3Prefix(storageProperties.getS3().getKeyPrefix());
        if (keyPrefix == null) {
            return relative;
        }
        return keyPrefix + "/" + relative;
    }

    private String buildAccessPath(AppDocument document) {
        if (isPublicDocument(document)) {
            return "/api/documents/" + document.getId() + "/content";
        }
        return "/api/admin/documents/" + document.getId() + "/content";
    }

    private String buildPendingPath(String objectKey) {
        return "pending:" + objectKey;
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
        if (type.contains("png")) {
            return ".png";
        }
        if (type.contains("webp")) {
            return ".webp";
        }
        if (type.contains("gif")) {
            return ".gif";
        }
        if (type.contains("pdf")) {
            return ".pdf";
        }
        if (type.contains("wordprocessingml")) {
            return ".docx";
        }
        if (type.contains("msword")) {
            return ".doc";
        }
        if (type.contains("jpeg") || type.contains("jpg")) {
            return ".jpg";
        }
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

    private String normalizeS3Prefix(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        String clean = normalized
                .replace('\\', '/')
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
        return clean.isBlank() ? null : clean;
    }

    private String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException("Unable to calculate document checksum");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte current : bytes) {
            builder.append(Character.forDigit((current >> 4) & 0xF, 16));
            builder.append(Character.forDigit(current & 0xF, 16));
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
