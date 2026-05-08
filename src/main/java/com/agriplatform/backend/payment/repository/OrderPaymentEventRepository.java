package com.agriplatform.backend.payment.repository;

import com.agriplatform.backend.payment.model.OrderPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentEventRepository extends JpaRepository<OrderPaymentEvent, Long> {
}
