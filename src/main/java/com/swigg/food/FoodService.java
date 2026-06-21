package com.swigg.food;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodService {

    private static final Logger logger = LoggerFactory.getLogger(FoodService.class);

    @Autowired
    private FoodRepository foodRepository;

    /**
     * Create a new food item (Only restaurants can do this)
     */
    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public FoodResponseDTO createFood(UUID restaurantId, FoodRequestDTO request) {
        logger.info("Creating food for restaurant: {}", restaurantId);

        if (restaurantId == null) {
            logger.warn("Cannot create food: restaurantId is null");
            throw new IllegalArgumentException("Restaurant ID is required");
        }

        Food food = Food.builder()
                .foodName(request.getFoodName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .restaurantId(restaurantId)
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isActive(true)
                .rating(java.math.BigDecimal.ZERO)
                .reviewCount(0)
                .build();

        Food savedFood = foodRepository.save(food);
        logger.info("Food created successfully with ID: {} for restaurant: {}", savedFood.getFoodId(), restaurantId);

        return mapToResponseDTO(savedFood);
    }

    /**
     * Update food item (Only restaurants can do this) - Partial update
     */
    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public FoodResponseDTO updateFood(UUID restaurantId, UUID foodId, FoodUpdateRequestDTO request) {
        logger.info("Updating food: {} for restaurant: {}", foodId, restaurantId);

        Food food = foodRepository.findByFoodIdAndRestaurantId(foodId, restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Food not found: {} for restaurant: {}", foodId, restaurantId);
                    return new IllegalArgumentException("Food not found or does not belong to this restaurant");
                });

        if (request.getFoodName() != null) {
            food.setFoodName(request.getFoodName());
        }
        if (request.getDescription() != null) {
            food.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            food.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            food.setCategory(request.getCategory());
        }
        if (request.getIsAvailable() != null) {
            food.setIsAvailable(request.getIsAvailable());
        }

        Food updatedFood = foodRepository.save(food);
        logger.info("Food updated successfully: {}", foodId);

        return mapToResponseDTO(updatedFood);
    }

    /**
     * Set food availability (Only restaurants can do this)
     */
    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public FoodResponseDTO setAvailability(UUID restaurantId, UUID foodId, Boolean isAvailable) {
        logger.info("Setting availability for food: {} in restaurant: {} to: {}", foodId, restaurantId, isAvailable);

        Food food = foodRepository.findByFoodIdAndRestaurantId(foodId, restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Food not found: {} for restaurant: {}", foodId, restaurantId);
                    return new IllegalArgumentException("Food not found or does not belong to this restaurant");
                });

        food.setIsAvailable(isAvailable);
        Food updatedFood = foodRepository.save(food);
        logger.info("Food availability set: {} - isAvailable: {}", foodId, updatedFood.getIsAvailable());

        return mapToResponseDTO(updatedFood);
    }

    /**
     * Delete food item (Only restaurants can do this)
     */
    @Transactional
    @CacheEvict(value = "foods", allEntries = true)
    public void deleteFood(UUID restaurantId, UUID foodId) {
        logger.info("Deleting food: {} from restaurant: {}", foodId, restaurantId);

        Food food = foodRepository.findByFoodIdAndRestaurantId(foodId, restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Food not found: {} for restaurant: {}", foodId, restaurantId);
                    return new IllegalArgumentException("Food not found or does not belong to this restaurant");
                });

        food.setIsActive(false);
        foodRepository.save(food);
        logger.info("Food deactivated: {}", foodId);
    }

    /**
     * Get all foods for a restaurant (Cached) - All active foods
     */
    @Cacheable(value = "foods", key = "'restaurant_' + #restaurantId")
    public List<FoodResponseDTO> getFoodsByRestaurant(UUID restaurantId) {
        logger.info("Fetching foods for restaurant: {}", restaurantId);

        List<Food> foods = foodRepository.findByRestaurantIdAndIsActive(restaurantId, true);
        logger.info("Found {} active foods for restaurant: {}", foods.size(), restaurantId);

        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available foods for a restaurant (Cached)
     */
    @Cacheable(value = "foods", key = "'restaurant_available_' + #restaurantId")
    public List<FoodResponseDTO> getAvailableFoodsByRestaurant(UUID restaurantId) {
        logger.info("Fetching available foods for restaurant: {}", restaurantId);

        List<Food> foods = foodRepository.findByRestaurantIdAndIsActiveAndIsAvailable(restaurantId, true, true);
        logger.info("Found {} available foods for restaurant: {}", foods.size(), restaurantId);

        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get foods by category (Cached) - Only available foods
     */
    @Cacheable(value = "foods", key = "'category_' + #restaurantId + '_' + #category")
    public List<FoodResponseDTO> getFoodsByCategory(UUID restaurantId, FoodCategory category) {
        logger.info("Fetching foods for category: {} in restaurant: {}", category, restaurantId);

        List<Food> foods = foodRepository.findByRestaurantIdAndCategoryAndIsActive(restaurantId, category, true)
                .stream()
                .filter(food -> Boolean.TRUE.equals(food.getIsAvailable()))
                .collect(Collectors.toList());
        logger.info("Found {} available foods for category: {} in restaurant: {}", foods.size(), category, restaurantId);

        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get single food by ID (Cached) - Only available foods
     */
    @Cacheable(value = "foods", key = "'food_' + #foodId")
    public FoodResponseDTO getFoodById(UUID foodId) {
        logger.info("Fetching food: {}", foodId);

        Food food = foodRepository.findByFoodIdAndIsActive(foodId, true)
                .orElseThrow(() -> {
                    logger.warn("Food not found or inactive: {}", foodId);
                    return new IllegalArgumentException("Food not found");
                });

        if (!Boolean.TRUE.equals(food.getIsAvailable())) {
            logger.warn("Food is not available: {}", foodId);
            throw new IllegalArgumentException("Food is not available");
        }

        return mapToResponseDTO(food);
    }

    /**
     * Get foods sorted by rating (Cached) - Only available foods
     */
    @Cacheable(value = "foods", key = "'category_rated_' + #restaurantId + '_' + #category")
    public List<FoodResponseDTO> getTopRatedFoodsByCategory(UUID restaurantId, FoodCategory category) {
        logger.info("Fetching top-rated foods for category: {} in restaurant: {}", category, restaurantId);

        List<Food> foods = foodRepository.findByCategoryWithHighestRating(restaurantId, category)
                .stream()
                .filter(food -> Boolean.TRUE.equals(food.getIsAvailable()))
                .collect(Collectors.toList());
        logger.info("Found {} top-rated available foods for category: {}", foods.size(), category);

        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Food entity to FoodResponseDTO
     */
    private FoodResponseDTO mapToResponseDTO(Food food) {
        return FoodResponseDTO.builder()
                .foodId(food.getFoodId())
                .foodName(food.getFoodName())
                .description(food.getDescription())
                .price(food.getPrice())
                .rating(food.getRating())
                .reviewCount(food.getReviewCount())
                .category(food.getCategory())
                .restaurantId(food.getRestaurantId())
                .isAvailable(food.getIsAvailable())
                .isActive(food.getIsActive())
                .createdAt(food.getCreatedAt())
                .updatedAt(food.getUpdatedAt())
                .build();
    }

    /**
     * Get all available foods for customers (from all restaurants)
     */
    @Cacheable(value = "foods", key = "'all_available'")
    public List<FoodResponseDTO> getAllAvailableFoods() {
        logger.info("Fetching all available foods for customers");

        List<Food> foods = foodRepository.findByIsActiveAndIsAvailable(true, true);
        logger.info("Found {} available foods for customers", foods.size());

        return foods.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}
