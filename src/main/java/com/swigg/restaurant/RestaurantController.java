package com.swigg.restaurant;

import com.swigg.auth.AuthService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private AuthService authService;

    @PostMapping("/register/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerRequest(Authentication authentication, @ModelAttribute RestaurantRegisterRequestDTO request) {
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
            
            // Generate new JWT tokens with RESTAURANT role
            User user = restaurant.getUser();
            TokenResponseDTO tokens = authService.generateTokensForUser(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Restaurant registered and verified successfully",
                    "restaurantId", restaurant.getRestaurantId(),
                    "accessToken", tokens.getAccessToken(),
                    "refreshToken", tokens.getRefreshToken()
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
            Restaurant tempRestaurant = restaurantRepository.findByUser_PhoneNumberAndUser_IsActive(request.getPhoneNumber(), true)
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

    @PostMapping("/update/image")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<?> updateRestaurantImage(Authentication authentication, @RequestParam("imageFile") MultipartFile imageFile) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Restaurant image update request received for userId: {}", userId);
        try {
            Restaurant updatedRestaurant = restaurantService.updateRestaurantImage(userId, imageFile);
            logger.info("Restaurant image updated successfully for userId: {}", userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Restaurant image updated successfully",
                    "restaurantId", updatedRestaurant.getRestaurantId()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant image update failed for userId: {}. Reason: {}", userId, e.getMessage());
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

    @GetMapping("/user")
    public ResponseEntity<?> getRestaurantByAuth() {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        logger.info("Get restaurant request received for userId from auth: {}", userId);
        try {
            Restaurant restaurant = restaurantService.getRestaurantByUserId(userId);
            logger.info("Successfully fetched restaurant for userId: {}", userId);
            return ResponseEntity.ok(restaurant);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to fetch restaurant for userId: {}. Reason: {}", userId, e.getMessage());
            if (e.getMessage().contains("not available")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
