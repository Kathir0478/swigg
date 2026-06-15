package com.swigg.food;

import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
    public ResponseEntity<ApiResponse<FoodResponseDTO>> createFood(
            @Valid @RequestBody FoodRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Food creation request received");
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.createFood(restaurantId, request);
            return ApiResponses.created("Food created successfully", food);
        } catch (IllegalArgumentException e) {
            logger.warn("Food creation failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Food creation error", e);
            return ApiResponses.internalError();
        }
    }

    @PutMapping("/{foodId}/update")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<FoodResponseDTO>> updateFood(
            @PathVariable UUID foodId,
            @Valid @RequestBody FoodRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Food update request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.updateFood(restaurantId, foodId, request);
            return ApiResponses.ok("Food updated successfully", food);
        } catch (IllegalArgumentException e) {
            logger.warn("Food update failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Food update error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/{foodId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> deleteFood(
            @PathVariable UUID foodId,
            Authentication authentication) {
        try {
            logger.info("Food deletion request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            foodService.deleteFood(restaurantId, foodId);
            return ApiResponses.ok("Food deleted successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Food deletion failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Food deletion error", e);
            return ApiResponses.internalError();
        }
    }

    @PatchMapping("/{foodId}/availability")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<FoodResponseDTO>> toggleAvailability(
            @PathVariable UUID foodId,
            Authentication authentication) {
        try {
            logger.info("Food availability toggle request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.toggleAvailability(restaurantId, foodId);
            return ApiResponses.ok("Food availability toggled successfully", food);
        } catch (IllegalArgumentException e) {
            logger.warn("Availability toggle failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Availability toggle error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'restaurant_' + #restaurantId")
    public ResponseEntity<ApiResponse<List<FoodResponseDTO>>> getFoodsByRestaurant(
            @PathVariable UUID restaurantId) {
        try {
            logger.info("Fetching foods for restaurant: {}", restaurantId);
            List<FoodResponseDTO> foods = foodService.getFoodsByRestaurant(restaurantId);
            return ApiResponses.ok("Foods fetched successfully", foods, foods.size());
        } catch (Exception e) {
            logger.error("Error fetching foods for restaurant", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'restaurant_available_' + #restaurantId")
    public ResponseEntity<ApiResponse<List<FoodResponseDTO>>> getAvailableFoodsByRestaurant(
            @PathVariable UUID restaurantId) {
        try {
            logger.info("Fetching available foods for restaurant: {}", restaurantId);
            List<FoodResponseDTO> foods = foodService.getAvailableFoodsByRestaurant(restaurantId);
            return ApiResponses.ok("Available foods fetched successfully", foods, foods.size());
        } catch (Exception e) {
            logger.error("Error fetching available foods", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'category_' + #restaurantId + '_' + #category")
    public ResponseEntity<ApiResponse<List<FoodResponseDTO>>> getFoodsByCategory(
            @PathVariable UUID restaurantId,
            @PathVariable String category) {
        try {
            logger.info("Fetching foods by category: {} for restaurant: {}", category, restaurantId);
            FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
            List<FoodResponseDTO> foods = foodService.getFoodsByCategory(restaurantId, foodCategory);
            return ApiResponses.ok("Foods fetched successfully", foods, foods.size());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category: {}", category);
            return ApiResponses.badRequest("Invalid category: " + category);
        } catch (Exception e) {
            logger.error("Error fetching foods by category", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/{foodId}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'food_' + #foodId")
    public ResponseEntity<ApiResponse<FoodResponseDTO>> getFoodById(@PathVariable UUID foodId) {
        try {
            logger.info("Fetching food: {}", foodId);
            FoodResponseDTO food = foodService.getFoodById(foodId);
            return ApiResponses.ok("Food fetched successfully", food);
        } catch (IllegalArgumentException e) {
            logger.warn("Food not found: {}", foodId);
            return ApiResponses.notFound(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching food", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}/rated")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'category_rated_' + #restaurantId + '_' + #category")
    public ResponseEntity<ApiResponse<List<FoodResponseDTO>>> getTopRatedFoodsByCategory(
            @PathVariable UUID restaurantId,
            @PathVariable String category) {
        try {
            logger.info("Fetching top-rated foods by category: {} for restaurant: {}", category, restaurantId);
            FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
            List<FoodResponseDTO> foods = foodService.getTopRatedFoodsByCategory(restaurantId, foodCategory);
            return ApiResponses.ok("Top-rated foods fetched successfully", foods, foods.size());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category: {}", category);
            return ApiResponses.badRequest("Invalid category: " + category);
        } catch (Exception e) {
            logger.error("Error fetching top-rated foods", e);
            return ApiResponses.internalError();
        }
    }
}
