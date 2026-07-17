package com.agriplatform.backend.order.repository;

import com.agriplatform.backend.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Modifying
    @Query("update OrderItem item set item.product = null where item.product.id = :productId")
    int clearProductReference(@Param("productId") Long productId);
}
