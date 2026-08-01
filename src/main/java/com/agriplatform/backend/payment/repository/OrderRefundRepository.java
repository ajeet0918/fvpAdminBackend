package com.agriplatform.backend.payment.repository;

import com.agriplatform.backend.payment.model.OrderRefund;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRefundRepository extends JpaRepository<OrderRefund, Long> {
    List<OrderRefund> findByPurchaseOrderIdOrderByCreatedAtDesc(Long purchaseOrderId);

    Optional<OrderRefund> findByRefundId(String refundId);

    Optional<OrderRefund> findByProviderRefundId(String providerRefundId);
}
