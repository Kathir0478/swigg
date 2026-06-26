package com.swigg.cart;

import com.swigg.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * CREATE OR GET CART - Customer protected endpoint
     * Creates a new cart or retrieves existing active cart for the customer with the specified restaurant
     * Input: restaurantId only (customerId derived from JWT token via userId)
     * If no active cart exists with correct state (not ordered), creates new cart
     * If active cart exists, retrieves existing cart with all details
     * Auth: CUSTOMER role only
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> createOrGetCart(
            @Valid @RequestBody CartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Cart create-or-get request received for restaurant: {}", request.getRestaurantId());
            UUID userId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.createOrGetCart(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Cart retrieved/created successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Cart create-or-get failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Cart create-or-get error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    /**
     * ADD FOOD TO CART - Customer protected endpoint
     * Adds food item to the cart with price calculation
     * Input: foodId, price (totalPrice calculated automatically)
     * Auth: CUSTOMER role only
     */
    @PostMapping("/{cartId}/items/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddFoodToCartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Adding food to cart: {} with foodId: {}", cartId, request.getFoodId());
            UUID userId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.addFoodToCart(userId, cartId, request);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Food added to cart successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Add food to cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Add food to cart error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    /**
     * REMOVE FOOD FROM CART - Customer protected endpoint
     * Removes food item from cart with count
     * Input: foodId, count (number of items to remove)
     * Auth: CUSTOMER role only
     */
    @DeleteMapping("/{cartId}/items/remove")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeFoodFromCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody RemoveFoodFromCartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Removing food from cart: {} with foodId: {} count: {}", cartId, request.getFoodId(), request.getCount());
            UUID userId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.removeFoodFromCart(userId, cartId, request);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Food removed from cart successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Remove food from cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Remove food from cart error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    /**
     * GET CART - Customer protected endpoint
     * Retrieves active cart details by customerId and restaurantId
     * Input: customerId, restaurantId (customerId can be derived from JWT token)
     * Auth: CUSTOMER role only
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(
            @RequestParam UUID customerId,
            @RequestParam UUID restaurantId,
            Authentication authentication) {
        try {
            logger.info("Fetching active cart for customerId: {} and restaurantId: {}", customerId, restaurantId);
            UUID userId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.getActiveCartByCustomerAndRestaurant(userId, customerId, restaurantId);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Cart fetched successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Cart fetch failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Cart fetch error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    /**
     * SEARCH CART WITH FOOD DETAILS - Customer protected endpoint
     * Retrieves active cart with complete food details by userId (from JWT) and restaurantId
     * Input: restaurantId (userId derived from JWT token)
     * Auth: CUSTOMER role only
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartWithFoodDetailsResponseDTO>> searchCartWithFoodDetails(
            @RequestParam UUID restaurantId,
            Authentication authentication) {
        try {
            logger.info("Searching cart with food details for restaurantId: {}", restaurantId);
            UUID userId = UUID.fromString(authentication.getName());
            CartWithFoodDetailsResponseDTO cart = cartService.searchCartWithFoodDetails(userId, restaurantId);

            return ResponseEntity.ok(
                    ApiResponse.<CartWithFoodDetailsResponseDTO>builder()
                            .success(true)
                            .message("Cart with food details fetched successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Cart search failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<CartWithFoodDetailsResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Cart search error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartWithFoodDetailsResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    /**
     * DELETE CART - Customer protected endpoint
     * Deletes/deactivates cart by cartId
     * Auth: CUSTOMER role only
     */
    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteCart(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Deleting cart: {}", cartId);
            UUID userId = UUID.fromString(authentication.getName());
            cartService.deleteCart(userId, cartId);

            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Cart deleted successfully")
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Delete cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Delete cart error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }
}
