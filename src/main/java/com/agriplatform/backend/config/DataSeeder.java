package com.agriplatform.backend.config;

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

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(CategoryRepository categoryRepository, ProductRepository productRepository) {
        return args -> {
            if (categoryRepository.count() > 0 || productRepository.count() > 0) {
                return;
            }

            Category seeds = categoryRepository.save(new Category(
                    "Certified Seeds",
                    "certified-seeds",
                    "Seed inventory for grains, pulses, and high-volume crop planning."
            ));

            Category nutrition = categoryRepository.save(new Category(
                    "Crop Nutrition",
                    "crop-nutrition",
                    "Balanced fertilizers and soil treatment inputs for agricultural buyers."
            ));

            Category equipment = categoryRepository.save(new Category(
                    "Farm Equipment",
                    "farm-equipment",
                    "Commercial tools and field equipment for wholesalers and resellers."
            ));

            productRepository.save(new Product(
                    "Golden Wheat Seed Pack",
                    "golden-wheat-seed-pack",
                    "SEED-WHT-001",
                    new BigDecimal("1450.00"),
                    "kg",
                    new BigDecimal("5.00"),
                    new BigDecimal("0.00"),
                    ProductStatus.ACTIVE,
                    "/assets/product-seeds.jpg",
                    null,
                    null,
                    null,
                    "High-germination wheat seed for commercial procurement.",
                    "A moisture-tested grain seed pack designed for large acreage planting and consistent yield performance.",
                    "250 kg",
                    true,
                    seeds
            ));

            productRepository.save(new Product(
                    "BioRich Organic Fertilizer",
                    "biorich-organic-fertilizer",
                    "FERT-ORG-021",
                    new BigDecimal("980.00"),
                    "bag",
                    new BigDecimal("5.00"),
                    new BigDecimal("2.00"),
                    ProductStatus.ACTIVE,
                    "/assets/product-fertilizer.jpg",
                    null,
                    null,
                    null,
                    "Organic crop nutrition blend for soil recovery and crop vigor.",
                    "A balanced fertilizer mix for wholesale buyers seeking farm-ready nutrition solutions with sustainable positioning.",
                    "500 bags",
                    true,
                    nutrition
            ));

            productRepository.save(new Product(
                    "FieldPro Sprayer Kit",
                    "fieldpro-sprayer-kit",
                    "EQUIP-SPR-004",
                    new BigDecimal("5200.00"),
                    "unit",
                    new BigDecimal("12.00"),
                    new BigDecimal("3.00"),
                    ProductStatus.ACTIVE,
                    "/assets/product-equipment.jpg",
                    null,
                    null,
                    null,
                    "Commercial crop care equipment for spraying operations.",
                    "A field-ready equipment kit intended for agriculture distributors and bulk equipment procurement workflows.",
                    "50 units",
                    false,
                    equipment
            ));
        };
    }
}
