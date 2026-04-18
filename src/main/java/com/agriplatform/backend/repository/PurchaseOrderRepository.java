package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.PurchaseOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    List<PurchaseOrder> findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(String email, String phone);
    List<PurchaseOrder> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
}
