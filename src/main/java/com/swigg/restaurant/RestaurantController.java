package com.swigg.restaurant;

import com.swigg.auth.TokenResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @PostMapping("/register/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerRequest(Authentication authentication, @RequestBody RestaurantRegisterRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Restaurant registration request received for userId: {}", userId);
        try {
            RestaurantInitResponseDTO response = restaurantService.initiateRegister(userId, request);
            logger.info("Restaurant registration TOTP code generated for userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant registration request failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerVerify(Authentication authentication, @RequestBody RestaurantRegisterVerifyRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Restaurant registration verification request received for userId: {}", userId);
        try {
            Restaurant restaurant = restaurantService.completeRegister(userId, request.getOtpCode());
            logger.info("Restaurant registration verified for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Restaurant registered and verified successfully",
                    "restaurantId", restaurant.getRestaurantId()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant registration verification failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<?> loginRequest(@RequestBody RestaurantLoginRequestDTO request) {
        logger.info("Restaurant login request received for username: {}", request.getUsername());
        try {
            RestaurantInitResponseDTO response = restaurantService.initiateLogin(request);
            logger.info("Restaurant login TOTP code generated for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant login request failed for username: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@RequestBody RestaurantLoginVerifyRequestDTO request) {
        logger.info("Restaurant login verification request received for phone: {}", request.getPhoneNumber());
        try {
            Restaurant tempRestaurant = restaurantRepository.findByUser_PhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            TokenResponseDTO response = restaurantService.completeLogin(tempRestaurant.getRestaurantId(), request.getOtpCode());
            logger.info("Restaurant login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant login verification failed for phone: {}. Reason: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/delete/request")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> deleteRequest(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Restaurant delete OTP request received for restaurantId: {}", userId);
        try {
            var response = restaurantService.requestDeletion(userId);
            logger.info("Restaurant delete OTP sent for restaurantId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant delete OTP request failed for restaurantId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/delete/complete")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> deleteComplete(Authentication authentication, @RequestBody RestaurantDeleteVerifyDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Restaurant delete completion request received for userId: {}", userId);
        try {
            restaurantService.completeDelete(userId, request);
            logger.info("Restaurant deleted successfully for userId: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Restaurant account deactivated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant deletion failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> updateRestaurant(Authentication authentication, @RequestBody RestaurantUpdateRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Restaurant update request received for userId: {}", userId);
        try {
            Restaurant updatedRestaurant = restaurantService.updateRestaurant(userId, request);
            logger.info("Restaurant updated successfully for userId: {}", userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Restaurant updated successfully",
                    "restaurantId", updatedRestaurant.getRestaurantId()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant update failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listRestaurants() {
        logger.info("List restaurants request received");
        try {
            List<Restaurant> restaurants = restaurantService.listAllRestaurants();
            logger.info("Successfully fetched {} restaurants", restaurants.size());
            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            logger.warn("Failed to list restaurants. Reason: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<?> getRestaurant(@PathVariable UUID restaurantId) {
        logger.info("Get restaurant request received for restaurantId: {}", restaurantId);
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
            logger.info("Successfully fetched restaurant for restaurantId: {}", restaurantId);
            return ResponseEntity.ok(restaurant);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to fetch restaurant for restaurantId: {}. Reason: {}", restaurantId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
