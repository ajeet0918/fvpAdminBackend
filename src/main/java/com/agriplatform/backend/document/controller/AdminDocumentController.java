package com.agriplatform.backend.document.controller;

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

import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/documents")
public class AdminDocumentController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminDocumentController.class);

    private final DocumentService documentService;

    public AdminDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable UUID id) {
        return map(documentService.getById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id) {
        AppDocument document = documentService.getById(id);
        Resource resource = documentService.download(id);
        return buildFileResponse(document, resource, true);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> streamDocument(@PathVariable UUID id) {
        AppDocument document = documentService.getById(id);
        Resource resource = documentService.download(id);
        return buildFileResponse(document, resource, false);
    }

    private ResponseEntity<Resource> buildFileResponse(AppDocument document, Resource resource, boolean attachment) {

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (document.getContentType() != null && !document.getContentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(document.getContentType());
            } catch (RuntimeException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition.Builder dispositionBuilder = attachment
                ? ContentDisposition.attachment()
                : ContentDisposition.inline();
        ContentDisposition contentDisposition = dispositionBuilder
                .filename(document.getOriginalFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @PutMapping("/{id}")
    public DocumentResponse updateDocument(@PathVariable UUID id, @Valid @RequestBody UpdateDocumentRequest request) {
        AppDocument updated = documentService.updateMetadata(
                id,
                request.originalFileName(),
                request.module(),
                request.ownerId()
        );
        return map(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean deletePhysical
    ) {
        documentService.delete(id, deletePhysical);
    }

    private DocumentResponse map(AppDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getOriginalFileName(),
                document.getObjectKey(),
                document.getPath(),
                document.getStorageProvider(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getChecksumSha256(),
                document.getModule(),
                document.getOwnerId(),
                document.getStatus().name(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

