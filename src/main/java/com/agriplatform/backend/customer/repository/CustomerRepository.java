package com.agriplatform.backend.customer.repository;

import com.agriplatform.backend.customer.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailIgnoreCaseAndPhone(String email, String phone);
    Optional<Customer> findByEmailIgnoreCase(String email);
    Optional<Customer> findByGoogleSubject(String googleSubject);
}
