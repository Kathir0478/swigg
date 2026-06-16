package com.swigg.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderIdAndCustomerId(UUID orderId, UUID customerId);

    Optional<Order> findByOrderIdAndRiderId(UUID orderId, UUID riderId);

    Optional<Order> findByOrderIdAndRestaurantId(UUID orderId, UUID restaurantId);

    List<Order> findByCustomerIdAndIsActive(UUID customerId, Boolean isActive);

    List<Order> findByRiderIdAndIsActive(UUID riderId, Boolean isActive);

    List<Order> findByRestaurantIdAndIsActive(UUID restaurantId, Boolean isActive);

    Optional<Order> findByOrderIdAndIsActive(UUID orderId, Boolean isActive);
}
