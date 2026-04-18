package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailIgnoreCaseAndPhone(String email, String phone);
}
