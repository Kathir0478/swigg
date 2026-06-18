package com.swigg.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findByUser_UserName(String userName);
    Optional<Restaurant> findByUser_PhoneNumber(String phoneNumber);
    Optional<Restaurant> findByUser_PhoneNumberAndUser_IsActive(String phoneNumber, Boolean isActive);
    Optional<Restaurant> findByUserId(UUID userId);
}
