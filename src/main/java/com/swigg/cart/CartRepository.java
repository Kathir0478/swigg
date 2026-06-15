package com.swigg.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByCartIdAndCustomerId(UUID cartId, UUID customerId);

    Optional<Cart> findByCustomerIdAndStatusAndIsActive(UUID customerId, CartStatus status, Boolean isActive);

    List<Cart> findByCustomerIdAndIsActive(UUID customerId, Boolean isActive);

    List<Cart> findByRestaurantIdAndStatusAndIsActive(UUID restaurantId, CartStatus status, Boolean isActive);

    @Query("SELECT c FROM Cart c WHERE c.customerId = ?1 AND c.restaurantId = ?2 AND c.status = 'ACTIVE' AND c.isActive = true")
    Optional<Cart> findActiveCartByCustomerAndRestaurant(UUID customerId, UUID restaurantId);
}
