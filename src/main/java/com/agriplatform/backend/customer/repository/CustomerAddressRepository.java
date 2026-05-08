package com.agriplatform.backend.customer.repository;

import com.agriplatform.backend.customer.model.CustomerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomer_IdOrderByIsDefaultDescUpdatedAtDesc(Long customerId);
    Optional<CustomerAddress> findByIdAndCustomer_Id(Long id, Long customerId);
}
