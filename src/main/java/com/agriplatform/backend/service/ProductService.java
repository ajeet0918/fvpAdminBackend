package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.ProductResponse;
import com.agriplatform.backend.model.Product;
import com.agriplatform.backend.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapProduct).toList();
    }

    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue().stream().map(this::mapProduct).toList();
    }

    public ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(this::mapProduct)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    private ProductResponse mapProduct(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getPrice(),
                product.getPriceUnit() != null ? product.getPriceUnit() : "kg",
                product.getStatus() != null ? product.getStatus().name() : "ACTIVE",
                product.getImageUrl(),
                product.getShortDescription(),
                product.getLongDescription(),
                product.getMoq(),
                product.isFeatured(),
                product.getCategory().getName()
        );
    }
}
