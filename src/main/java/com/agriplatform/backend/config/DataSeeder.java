package com.agriplatform.backend.config;

import com.agriplatform.backend.model.Category;
import com.agriplatform.backend.model.Product;
import com.agriplatform.backend.model.ProductStatus;
import com.agriplatform.backend.repository.CategoryRepository;
import com.agriplatform.backend.repository.ProductRepository;
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
                    "Commercial crop care equipment for spraying operations.",
                    "A field-ready equipment kit intended for agriculture distributors and bulk equipment procurement workflows.",
                    "50 units",
                    false,
                    equipment
            ));
        };
    }
}
