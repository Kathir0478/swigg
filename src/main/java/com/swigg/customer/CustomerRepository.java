package com.swigg.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUserId(UUID userId);
    Optional<Customer> findById(UUID customerId);
    Optional<Customer> findByUser_PhoneNumber(String phoneNumber);
    Optional<Customer> findByUser_PhoneNumberAndUser_IsActive(String phoneNumber, Boolean isActive);
    Optional<Customer> findByUser_UserName(String userName);
    List<Customer> findAll();
}
