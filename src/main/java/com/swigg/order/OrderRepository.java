package com.swigg.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderIdAndCustomerId(UUID orderId, UUID customerId);

    Optional<Order> findByOrderIdAndIsActive(UUID orderId, Boolean isActive);

    List<Order> findByCustomerIdAndIsActive(UUID customerId, Boolean isActive);

    List<Order> findByRiderIdAndIsActive(UUID riderId, Boolean isActive);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.isActive = true")
    List<Order> findByStatusAndIsActive(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.cartId = :cartId")
    Optional<Order> findByCartId(@Param("cartId") UUID cartId);
}
