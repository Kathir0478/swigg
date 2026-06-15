package com.swigg.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    /**
     * Find all active foods by restaurant ID
     */
    List<Food> findByRestaurantIdAndIsActive(UUID restaurantId, Boolean isActive);

    /**
     * Find all available foods by restaurant ID
     */
    List<Food> findByRestaurantIdAndIsAvailable(UUID restaurantId, Boolean isAvailable);

    /**
     * Find all active and available foods by restaurant ID
     */
    List<Food> findByRestaurantIdAndIsActiveAndIsAvailable(UUID restaurantId, Boolean isActive, Boolean isAvailable);

    /**
     * Find foods by category
     */
    List<Food> findByRestaurantIdAndCategoryAndIsActive(UUID restaurantId, FoodCategory category, Boolean isActive);

    /**
     * Find food by ID and restaurant ID (for authorization)
     */
    Optional<Food> findByFoodIdAndRestaurantId(UUID foodId, UUID restaurantId);

    /**
     * Find food by ID and ensure it's active
     */
    Optional<Food> findByFoodIdAndIsActive(UUID foodId, Boolean isActive);

    /**
     * Check if food exists for a restaurant
     */
    boolean existsByFoodIdAndRestaurantId(UUID foodId, UUID restaurantId);

    /**
     * Count active foods for a restaurant
     */
    long countByRestaurantIdAndIsActive(UUID restaurantId, Boolean isActive);

    /**
     * Find all foods by category
     */
    @Query("SELECT f FROM Food f WHERE f.restaurant.restaurantId = :restaurantId AND f.category = :category AND f.isActive = true ORDER BY f.rating DESC")
    List<Food> findByCategoryWithHighestRating(@Param("restaurantId") UUID restaurantId, @Param("category") FoodCategory category);
}
