package com.swigg.restaurant;

import com.swigg.auth.TokenResponseDTO;
import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<ApiResponse<RestaurantInitResponseDTO>> registerRequest(
            Authentication authentication,
            @RequestBody RestaurantRegisterRequestDTO request) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Restaurant registration request received for userId: {}", userId);
            RestaurantInitResponseDTO response = restaurantService.initiateRegister(userId, request);
            logger.info("Restaurant registration TOTP code generated for userId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant registration request failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant registration request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/register/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> registerVerify(
            Authentication authentication,
            @RequestBody RestaurantRegisterVerifyRequestDTO request) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Restaurant registration verification request received for userId: {}", userId);
            Restaurant restaurant = restaurantService.completeRegister(userId, request.getOtpCode());
            logger.info("Restaurant registration verified for userId: {}", userId);
            return ApiResponses.created("Restaurant registered and verified successfully", mapToResponseDTO(restaurant));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant registration verification failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant registration verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<ApiResponse<RestaurantInitResponseDTO>> loginRequest(
            @RequestBody RestaurantLoginRequestDTO request) {
        try {
            logger.info("Restaurant login request received for username: {}", request.getUsername());
            RestaurantInitResponseDTO response = restaurantService.initiateLogin(request);
            logger.info("Restaurant login TOTP code generated for username: {}", request.getUsername());
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant login request failed: {}", e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant login request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> loginVerify(
            @RequestBody RestaurantLoginVerifyRequestDTO request) {
        try {
            logger.info("Restaurant login verification request received for phone: {}", request.getPhoneNumber());
            Restaurant tempRestaurant = restaurantRepository.findByUser_PhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            TokenResponseDTO response = restaurantService.completeLogin(tempRestaurant.getRestaurantId(), request.getOtpCode());
            logger.info("Restaurant login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ApiResponses.ok("Login successful", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant login verification failed: {}", e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant login verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/delete/request")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(Authentication authentication) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Restaurant delete OTP request received for restaurantId: {}", userId);
            restaurantService.requestDeletion(userId);
            logger.info("Restaurant delete OTP sent for restaurantId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number");
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant delete OTP request failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant delete OTP request error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/delete/complete")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> deleteComplete(
            Authentication authentication,
            @RequestBody RestaurantDeleteVerifyDTO request) {
        try {
            UUID restaurantId = UUID.fromString(authentication.getName());
            logger.info("Restaurant delete completion request received for restaurantId: {}", restaurantId);
            restaurantService.completeDelete(restaurantId, request);
            logger.info("Restaurant deleted successfully for restaurantId: {}", restaurantId);
            return ApiResponses.ok("Restaurant account deactivated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant deletion failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant deletion error", e);
            return ApiResponses.internalError();
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> updateRestaurant(
            Authentication authentication,
            @RequestBody RestaurantUpdateRequestDTO request) {
        try {
            UUID restaurantId = UUID.fromString(authentication.getName());
            logger.info("Restaurant update request received for restaurantId: {}", restaurantId);
            Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, request);
            logger.info("Restaurant updated successfully for restaurantId: {}", restaurantId);
            return ApiResponses.ok("Restaurant updated successfully", mapToResponseDTO(updatedRestaurant));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant update failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant update error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDTO>>> listRestaurants() {
        try {
            logger.info("List restaurants request received");
            List<Restaurant> restaurants = restaurantService.listAllRestaurants();
            List<RestaurantResponseDTO> restaurantDTOs = restaurants.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
            logger.info("Successfully fetched {} restaurants", restaurants.size());
            return ApiResponses.ok("Restaurants fetched successfully", restaurantDTOs, restaurantDTOs.size());
        } catch (Exception e) {
            logger.error("List restaurants error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponseDTO>> getRestaurant(@PathVariable UUID restaurantId) {
        try {
            logger.info("Get restaurant request received for restaurantId: {}", restaurantId);
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
            logger.info("Successfully fetched restaurant for restaurantId: {}", restaurantId);
            return ApiResponses.ok("Restaurant fetched successfully", mapToResponseDTO(restaurant));
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant fetch failed: {}", e.getMessage());
            return ApiResponses.notFound(e.getMessage());
        } catch (Exception e) {
            logger.error("Restaurant fetch error", e);
            return ApiResponses.internalError();
        }
    }

    private RestaurantResponseDTO mapToResponseDTO(Restaurant restaurant) {
        return RestaurantResponseDTO.builder()
                .restaurantId(restaurant.getRestaurantId())
                .userId(restaurant.getUser().getUserId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .lat(restaurant.getLat())
                .lng(restaurant.getLng())
                .address(restaurant.getAddress())
                .openTime(restaurant.getOpenTime())
                .closeTime(restaurant.getCloseTime())
                .imageUrl(restaurant.getImageUrl())
                .isActive(restaurant.getIsActive())
                .isVerified(restaurant.getIsVerified())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}
