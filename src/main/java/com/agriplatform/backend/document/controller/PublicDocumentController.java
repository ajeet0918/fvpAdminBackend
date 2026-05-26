package com.agriplatform.backend.document.controller;

import com.agriplatform.backend.document.model.AppDocument;
import com.agriplatform.backend.document.model.DocumentStatus;
import com.agriplatform.backend.document.service.DocumentService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/documents")
public class PublicDocumentController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicDocumentController.class);

    private final DocumentService documentService;

    public PublicDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/public/products/{id}/content")
    public ResponseEntity<Resource> streamPublicProductDocument(@PathVariable UUID id) {
        return streamPublicDocument(id);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> streamLegacyPublicDocument(@PathVariable UUID id) {
        return streamPublicDocument(id);
    }

    private ResponseEntity<Resource> streamPublicDocument(UUID id) {
        AppDocument document = documentService.getById(id);
        if (document.getStatus() == DocumentStatus.DELETED) {
            throw new IllegalArgumentException("Document is deleted");
        }
        if (!documentService.isPublicDocument(document)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Document is not public");
        }

        Resource resource = documentService.download(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (document.getContentType() != null && !document.getContentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(document.getContentType());
            } catch (RuntimeException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename(document.getOriginalFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }
}
