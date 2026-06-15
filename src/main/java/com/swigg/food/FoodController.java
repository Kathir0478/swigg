package com.swigg.food;

import com.swigg.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);

    @Autowired
    private FoodService foodService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> createFood(
            @Valid @RequestBody FoodRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Food creation request received");
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.createFood(restaurantId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(true)
                            .message("Food created successfully")
                            .data(food)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Food creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Food creation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @PutMapping("/{foodId}/update")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> updateFood(
            @PathVariable UUID foodId,
            @Valid @RequestBody FoodRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Food update request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.updateFood(restaurantId, foodId, request);

            return ResponseEntity.ok(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(true)
                            .message("Food updated successfully")
                            .data(food)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Food update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Food update error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @DeleteMapping("/{foodId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> deleteFood(
            @PathVariable UUID foodId,
            Authentication authentication) {
        try {
            logger.info("Food deletion request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            foodService.deleteFood(restaurantId, foodId);

            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Food deleted successfully")
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Food deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Food deletion error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @PatchMapping("/{foodId}/availability")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> toggleAvailability(
            @PathVariable UUID foodId,
            Authentication authentication) {
        try {
            logger.info("Food availability toggle request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.toggleAvailability(restaurantId, foodId);

            return ResponseEntity.ok(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(true)
                            .message("Food availability toggled successfully")
                            .data(food)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Availability toggle failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Availability toggle error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'restaurant_' + #restaurantId")
    public ResponseEntity<?> getFoodsByRestaurant(
            @PathVariable UUID restaurantId) {
        try {
            logger.info("Fetching foods for restaurant: {}", restaurantId);
            List<FoodResponseDTO> foods = foodService.getFoodsByRestaurant(restaurantId);

            return ResponseEntity.ok(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(true)
                            .message("Foods fetched successfully")
                            .count(foods.size())
                            .data(foods)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching foods for restaurant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'restaurant_available_' + #restaurantId")
    public ResponseEntity<?> getAvailableFoodsByRestaurant(
            @PathVariable UUID restaurantId) {
        try {
            logger.info("Fetching available foods for restaurant: {}", restaurantId);
            List<FoodResponseDTO> foods = foodService.getAvailableFoodsByRestaurant(restaurantId);

            return ResponseEntity.ok(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(true)
                            .message("Available foods fetched successfully")
                            .count(foods.size())
                            .data(foods)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching available foods", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'category_' + #restaurantId + '_' + #category")
    public ResponseEntity<?> getFoodsByCategory(
            @PathVariable UUID restaurantId,
            @PathVariable String category) {
        try {
            logger.info("Fetching foods by category: {} for restaurant: {}", category, restaurantId);
            FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
            List<FoodResponseDTO> foods = foodService.getFoodsByCategory(restaurantId, foodCategory);

            return ResponseEntity.ok(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(true)
                            .message("Foods fetched successfully")
                            .count(foods.size())
                            .data(foods)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category: {}", category);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(false)
                            .message("Invalid category: " + category)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching foods by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/{foodId}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'food_' + #foodId")
    public ResponseEntity<?> getFoodById(
            @PathVariable UUID foodId) {
        try {
            logger.info("Fetching food: {}", foodId);
            FoodResponseDTO food = foodService.getFoodById(foodId);

            return ResponseEntity.ok(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(true)
                            .message("Food fetched successfully")
                            .data(food)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Food not found: {}", foodId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching food", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<FoodResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}/rated")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'category_rated_' + #restaurantId + '_' + #category")
    public ResponseEntity<?> getTopRatedFoodsByCategory(
            @PathVariable UUID restaurantId,
            @PathVariable String category) {
        try {
            logger.info("Fetching top-rated foods by category: {} for restaurant: {}", category, restaurantId);
            FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
            List<FoodResponseDTO> foods = foodService.getTopRatedFoodsByCategory(restaurantId, foodCategory);

            return ResponseEntity.ok(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(true)
                            .message("Top-rated foods fetched successfully")
                            .count(foods.size())
                            .data(foods)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category: {}", category);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(false)
                            .message("Invalid category: " + category)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error fetching top-rated foods", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<FoodResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }
}
