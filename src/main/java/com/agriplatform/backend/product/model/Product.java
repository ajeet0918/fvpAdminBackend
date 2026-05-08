package com.agriplatform.backend.product.model;

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

    @Column(length = 255)
    private String imageOriginalFileName;

    @Column(length = 120)
    private String imageContentType;

    private Long imageSizeBytes;

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
            String imageOriginalFileName,
            String imageContentType,
            Long imageSizeBytes,
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
        this.imageOriginalFileName = imageOriginalFileName;
        this.imageContentType = imageContentType;
        this.imageSizeBytes = imageSizeBytes;
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

    public String getImageOriginalFileName() {
        return imageOriginalFileName;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public Long getImageSizeBytes() {
        return imageSizeBytes;
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
            String imageOriginalFileName,
            String imageContentType,
            Long imageSizeBytes,
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
        this.imageOriginalFileName = imageOriginalFileName;
        this.imageContentType = imageContentType;
        this.imageSizeBytes = imageSizeBytes;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.moq = moq;
        this.featured = featured;
        this.category = category;
    }
}
