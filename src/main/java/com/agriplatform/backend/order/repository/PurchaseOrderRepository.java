package com.agriplatform.backend.order.repository;

import com.agriplatform.backend.order.model.PurchaseOrder;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    List<PurchaseOrder> findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(String email, String phone);
    List<PurchaseOrder> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    Optional<PurchaseOrder> findByPaymentProviderOrderId(String paymentProviderOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select purchaseOrder from PurchaseOrder purchaseOrder where purchaseOrder.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select purchaseOrder from PurchaseOrder purchaseOrder where purchaseOrder.orderNumber = :orderNumber")
    Optional<PurchaseOrder> findByOrderNumberForUpdate(@Param("orderNumber") String orderNumber);
}
