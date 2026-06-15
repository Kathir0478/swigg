package com.swigg.cart;

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

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> createCart(
            @Valid @RequestBody CartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Cart creation request received");
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.createCart(customerId, request);
            return ApiResponses.created("Cart created successfully", cart);
        } catch (IllegalArgumentException e) {
            logger.warn("Cart creation failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Cart creation error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Cacheable(value = "carts", key = "'cart_' + #cartId")
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Fetching cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.getCartById(customerId, cartId);
            return ApiResponses.ok("Cart fetched successfully", cart);
        } catch (IllegalArgumentException e) {
            logger.warn("Cart fetch failed: {}", e.getMessage());
            return ApiResponses.notFound(e.getMessage());
        } catch (Exception e) {
            logger.error("Cart fetch error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Cacheable(value = "carts", key = "'customer_active_' + #customerId")
    public ResponseEntity<ApiResponse<CartResponseDTO>> getActiveCart(Authentication authentication) {
        try {
            logger.info("Fetching active cart for customer");
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.getActiveCartByCustomer(customerId);
            return ApiResponses.ok("Active cart fetched successfully", cart);
        } catch (IllegalArgumentException e) {
            logger.warn("Active cart fetch failed: {}", e.getMessage());
            return ApiResponses.notFound(e.getMessage());
        } catch (Exception e) {
            logger.error("Active cart fetch error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/{cartId}/items/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddToCartRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Adding item to cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.addItemToCart(customerId, cartId, request);
            return ApiResponses.ok("Item added to cart successfully", cart);
        } catch (IllegalArgumentException e) {
            logger.warn("Add to cart failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Add to cart error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/{cartId}/items/{foodId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID foodId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        try {
            logger.info("Removing item from cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.removeItemFromCart(customerId, cartId, foodId, quantity);
            return ApiResponses.ok("Item removed from cart successfully", cart);
        } catch (IllegalArgumentException e) {
            logger.warn("Remove from cart failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Remove from cart error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/{cartId}/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponseDTO>> placeOrder(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Placing order for cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            CartResponseDTO cart = cartService.placeOrder(customerId, cartId);
            return ApiResponses.ok("Order placed successfully", cart);
        } catch (IllegalArgumentException e) {
            logger.warn("Place order failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Place order error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteCart(
            @PathVariable UUID cartId,
            Authentication authentication) {
        try {
            logger.info("Deleting cart: {}", cartId);
            UUID customerId = UUID.fromString(authentication.getName());
            cartService.deleteCart(customerId, cartId);
            return ApiResponses.ok("Cart deleted successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Delete cart failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Delete cart error", e);
            return ApiResponses.internalError();
        }
    }
}
