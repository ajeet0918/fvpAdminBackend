package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.AdminProductResponse;
import com.agriplatform.backend.dto.CreateProductRequest;
import com.agriplatform.backend.dto.UpdateProductRequest;
import com.agriplatform.backend.model.Category;
import com.agriplatform.backend.model.Product;
import com.agriplatform.backend.model.ProductStatus;
import com.agriplatform.backend.repository.CategoryRepository;
import com.agriplatform.backend.repository.ProductRepository;
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
