package com.swigg.cart;

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

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createCart(
            @Valid @RequestBody CartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Cart creation request received");
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.createCart(customerId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Cart created successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Cart creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Cart creation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Cacheable(value = "carts", key = "'cart_' + #cartId")
    public ResponseEntity<?> getCart(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Fetching cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.getCartById(customerId, cartId);

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

    @GetMapping("/active")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Cacheable(value = "carts", key = "'customer_active_' + #customerId")
    public ResponseEntity<?> getActiveCart(
            Authentication authentication) {
        try {
            logger.info("Fetching active cart for customer");
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.getActiveCartByCustomer(customerId);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Active cart fetched successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Active cart fetch failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Active cart fetch error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @PostMapping("/{cartId}/items/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddToCartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Adding item to cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.addItemToCart(customerId, cartId, request);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Item added to cart successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Add to cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Add to cart error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @DeleteMapping("/{cartId}/items/{foodId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID foodId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        try {
            logger.info("Removing item from cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.removeItemFromCart(customerId, cartId, foodId, quantity);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Item removed from cart successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Remove from cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Remove from cart error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @PostMapping("/{cartId}/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> placeOrder(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Placing order for cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.placeOrder(customerId, cartId);

            return ResponseEntity.ok(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(true)
                            .message("Order placed successfully")
                            .data(cart)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Place order failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Place order error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<CartResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteCart(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Deleting cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            cartService.deleteCart(customerId, cartId);

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
