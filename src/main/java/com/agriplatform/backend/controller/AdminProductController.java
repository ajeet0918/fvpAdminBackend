package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.AdminProductResponse;
import com.agriplatform.backend.dto.CreateProductRequest;
import com.agriplatform.backend.dto.ImageUploadResponse;
import com.agriplatform.backend.dto.UpdateProductRequest;
import com.agriplatform.backend.service.AdminProductService;
import com.agriplatform.backend.service.ProductImageStorageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final ProductImageStorageService productImageStorageService;

    public AdminProductController(
            AdminProductService adminProductService,
            ProductImageStorageService productImageStorageService
    ) {
        this.adminProductService = adminProductService;
        this.productImageStorageService = productImageStorageService;
    }

    @GetMapping
    public List<AdminProductResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId
    ) {
        return adminProductService.getProducts(search, status, categoryId);
    }

    @GetMapping("/{id}")
    public AdminProductResponse getProduct(@PathVariable Long id) {
        return adminProductService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return adminProductService.createProduct(request);
    }

    @PutMapping("/{id}")
    public AdminProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return adminProductService.updateProduct(id, request);
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse uploadProductImage(@RequestPart("file") MultipartFile file) {
        return productImageStorageService.store(file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
    }
}
