package com.swigg.food;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);

    @Autowired
    private FoodService foodService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<Map<String, Object>> createFood(
            @Valid @RequestBody FoodRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Food creation request received");
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.createFood(restaurantId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Food created successfully");
            response.put("data", food);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Food creation failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Food creation error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{foodId}/update")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<Map<String, Object>> updateFood(
            @PathVariable UUID foodId,
            @Valid @RequestBody FoodRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Food update request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.updateFood(restaurantId, foodId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Food updated successfully");
            response.put("data", food);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Food update failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Food update error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{foodId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<Map<String, Object>> deleteFood(
            @PathVariable UUID foodId,
            Authentication authentication) {
        try {
            logger.info("Food deletion request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            foodService.deleteFood(restaurantId, foodId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Food deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Food deletion failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Food deletion error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PatchMapping("/{foodId}/availability")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<Map<String, Object>> toggleAvailability(
            @PathVariable UUID foodId,
            Authentication authentication) {
        try {
            logger.info("Food availability toggle request received for foodId: {}", foodId);
            UUID restaurantId = UUID.fromString(authentication.getName());
            FoodResponseDTO food = foodService.toggleAvailability(restaurantId, foodId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Food availability toggled successfully");
            response.put("data", food);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Availability toggle failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Availability toggle error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'restaurant_' + #restaurantId")
    public ResponseEntity<Map<String, Object>> getFoodsByRestaurant(
            @PathVariable UUID restaurantId) {
        try {
            logger.info("Fetching foods for restaurant: {}", restaurantId);
            List<FoodResponseDTO> foods = foodService.getFoodsByRestaurant(restaurantId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Foods fetched successfully");
            response.put("count", foods.size());
            response.put("data", foods);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching foods for restaurant", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'restaurant_available_' + #restaurantId")
    public ResponseEntity<Map<String, Object>> getAvailableFoodsByRestaurant(
            @PathVariable UUID restaurantId) {
        try {
            logger.info("Fetching available foods for restaurant: {}", restaurantId);
            List<FoodResponseDTO> foods = foodService.getAvailableFoodsByRestaurant(restaurantId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Available foods fetched successfully");
            response.put("count", foods.size());
            response.put("data", foods);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching available foods", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'category_' + #restaurantId + '_' + #category")
    public ResponseEntity<Map<String, Object>> getFoodsByCategory(
            @PathVariable UUID restaurantId,
            @PathVariable String category) {
        try {
            logger.info("Fetching foods by category: {} for restaurant: {}", category, restaurantId);
            FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
            List<FoodResponseDTO> foods = foodService.getFoodsByCategory(restaurantId, foodCategory);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Foods fetched successfully");
            response.put("count", foods.size());
            response.put("data", foods);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category: {}", category);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid category: " + category);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error fetching foods by category", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{foodId}")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'food_' + #foodId")
    public ResponseEntity<Map<String, Object>> getFoodById(
            @PathVariable UUID foodId) {
        try {
            logger.info("Fetching food: {}", foodId);
            FoodResponseDTO food = foodService.getFoodById(foodId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Food fetched successfully");
            response.put("data", food);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Food not found: {}", foodId);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            logger.error("Error fetching food", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}/rated")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "foods", key = "'category_rated_' + #restaurantId + '_' + #category")
    public ResponseEntity<Map<String, Object>> getTopRatedFoodsByCategory(
            @PathVariable UUID restaurantId,
            @PathVariable String category) {
        try {
            logger.info("Fetching top-rated foods by category: {} for restaurant: {}", category, restaurantId);
            FoodCategory foodCategory = FoodCategory.valueOf(category.toUpperCase());
            List<FoodResponseDTO> foods = foodService.getTopRatedFoodsByCategory(restaurantId, foodCategory);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Top-rated foods fetched successfully");
            response.put("count", foods.size());
            response.put("data", foods);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category: {}", category);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Invalid category: " + category);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error fetching top-rated foods", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
