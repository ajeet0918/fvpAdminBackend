package com.agriplatform.backend.product.service;

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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> getProducts(String search, String status, Long categoryId) {
        String searchFilter = normalizeSearch(search);
        ProductStatus statusFilter = parseStatusNullable(status);

        return productRepository.findAll().stream()
                .filter(product -> matchesSearch(product, searchFilter))
                .filter(product -> statusFilter == null || product.getStatus() == statusFilter)
                .filter(product -> categoryId == null || product.getCategory().getId().equals(categoryId))
                .sorted(Comparator.comparing(Product::getName))
                .map(this::mapProduct)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapProduct(product);
    }

    @Transactional
    public AdminProductResponse createProduct(CreateProductRequest request) {
        String slug = normalizeSlug(request.slug());
        String sku = normalizeSku(request.sku());
        ProductStatus status = parseStatus(request.status());
        if (productRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Product slug already exists");
        }
        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Product SKU already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category"));

        Product product = new Product(
                request.name().trim(),
                slug,
                sku,
                request.price(),
                normalizePriceUnit(request.priceUnit()),
                request.defaultTaxRate(),
                request.defaultDiscountRate(),
                status,
                normalizeImageUrl(request.imageUrl()),
                normalizeNullable(request.imageOriginalFileName()),
                normalizeNullable(request.imageContentType()),
                request.imageSizeBytes(),
                request.shortDescription().trim(),
                request.longDescription().trim(),
                request.moq().trim(),
                request.featured(),
                category
        );
        return mapProduct(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String slug = normalizeSlug(request.slug());
        String sku = normalizeSku(request.sku());
        ProductStatus status = parseStatus(request.status());
        if (productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new IllegalArgumentException("Product slug already exists");
        }
        if (productRepository.existsBySkuAndIdNot(sku, id)) {
            throw new IllegalArgumentException("Product SKU already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category"));

        product.updateDetails(
                request.name().trim(),
                slug,
                sku,
                request.price(),
                normalizePriceUnit(request.priceUnit()),
                request.defaultTaxRate(),
                request.defaultDiscountRate(),
                status,
                normalizeImageUrl(request.imageUrl()),
                normalizeNullable(request.imageOriginalFileName()),
                normalizeNullable(request.imageContentType()),
                request.imageSizeBytes(),
                request.shortDescription().trim(),
                request.longDescription().trim(),
                request.moq().trim(),
                request.featured(),
                category
        );
        return mapProduct(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private AdminProductResponse mapProduct(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getPrice(),
                product.getPriceUnit() != null ? product.getPriceUnit() : "kg",
                product.getDefaultTaxRate() != null ? product.getDefaultTaxRate() : java.math.BigDecimal.ZERO,
                product.getDefaultDiscountRate() != null ? product.getDefaultDiscountRate() : java.math.BigDecimal.ZERO,
                product.getStatus() != null ? product.getStatus().name() : "ACTIVE",
                product.getImageUrl(),
                product.getImageOriginalFileName(),
                product.getImageContentType(),
                product.getImageSizeBytes(),
                product.getShortDescription(),
                product.getLongDescription(),
                product.getMoq(),
                product.isFeatured(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }

    private String normalizeSlug(String value) {
        return value.trim().toLowerCase();
    }

    private String normalizeSku(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizePriceUnit(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private ProductStatus parseStatus(String value) {
        try {
            return ProductStatus.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid product status");
        }
    }

    private ProductStatus parseStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseStatus(value);
    }

    private String normalizeImageUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private boolean matchesSearch(Product product, String search) {
        if (search == null) {
            return true;
        }
        return containsIgnoreCase(product.getName(), search)
                || containsIgnoreCase(product.getSlug(), search)
                || containsIgnoreCase(product.getSku(), search)
                || containsIgnoreCase(product.getShortDescription(), search);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }
}
