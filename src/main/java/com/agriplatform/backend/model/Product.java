package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 30)
    private String priceUnit;

    @Column(precision = 5, scale = 2)
    private BigDecimal defaultTaxRate;

    @Column(precision = 5, scale = 2)
    private BigDecimal defaultDiscountRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(length = 600)
    private String imageUrl;

    @Column(nullable = false, length = 300)
    private String shortDescription;

    @Column(nullable = false, length = 1200)
    private String longDescription;

    @Column(nullable = false)
    private String moq;

    @Column(nullable = false)
    private boolean featured;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    public Product() {
    }

    public Product(
            String name,
            String slug,
            String sku,
            BigDecimal price,
            String priceUnit,
            BigDecimal defaultTaxRate,
            BigDecimal defaultDiscountRate,
            ProductStatus status,
            String imageUrl,
            String shortDescription,
            String longDescription,
            String moq,
            boolean featured,
            Category category
    ) {
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.price = price;
        this.priceUnit = priceUnit;
        this.defaultTaxRate = defaultTaxRate;
        this.defaultDiscountRate = defaultDiscountRate;
        this.status = status;
        this.imageUrl = imageUrl;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.moq = moq;
        this.featured = featured;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public BigDecimal getDefaultTaxRate() {
        return defaultTaxRate;
    }

    public BigDecimal getDefaultDiscountRate() {
        return defaultDiscountRate;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getMoq() {
        return moq;
    }

    public boolean isFeatured() {
        return featured;
    }

    public Category getCategory() {
        return category;
    }

    public void updateDetails(
            String name,
            String slug,
            String sku,
            BigDecimal price,
            String priceUnit,
            BigDecimal defaultTaxRate,
            BigDecimal defaultDiscountRate,
            ProductStatus status,
            String imageUrl,
            String shortDescription,
            String longDescription,
            String moq,
            boolean featured,
            Category category
    ) {
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.price = price;
        this.priceUnit = priceUnit;
        this.defaultTaxRate = defaultTaxRate;
        this.defaultDiscountRate = defaultDiscountRate;
        this.status = status;
        this.imageUrl = imageUrl;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.moq = moq;
        this.featured = featured;
        this.category = category;
    }
}
