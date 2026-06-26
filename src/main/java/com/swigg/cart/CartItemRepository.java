package com.swigg.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndFoodId(UUID cartId, UUID foodId);

    List<CartItem> findByCartId(UUID cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.foodId = :foodId")
    Optional<CartItem> findByCartAndFood(@Param("cartId") UUID cartId, @Param("foodId") UUID foodId);

    void deleteByCartId(UUID cartId);
}
