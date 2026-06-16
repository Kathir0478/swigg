package com.swigg.order;

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
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Order creation request received");
            UUID customerId = UUID.fromString(authentication.getName());
            OrderResponseDTO order = orderService.createOrder(customerId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(true)
                            .message("Order created successfully")
                            .data(order)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Order creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Order creation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequestDTO request,
            Authentication authentication) {
        try {
            logger.info("Order update request received for order: {}", orderId);
            UUID customerId = UUID.fromString(authentication.getName());
            OrderResponseDTO order = orderService.updateOrder(customerId, orderId, request);

            return ResponseEntity.ok(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(true)
                            .message("Order updated successfully")
                            .data(order)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Order update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Order update error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RIDER', 'RESTAURANT', 'ADMIN')")
    @Cacheable(value = "orders", key = "'order_' + #orderId")
    public ResponseEntity<?> getOrderById(
            @PathVariable UUID orderId) {
        try {
            logger.info("Fetching order: {}", orderId);
            OrderResponseDTO order = orderService.getOrderById(orderId);

            return ResponseEntity.ok(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(true)
                            .message("Order fetched successfully")
                            .data(order)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Order fetch failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Order fetch error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Cacheable(value = "orders", key = "'customer_orders_' + #customerId")
    public ResponseEntity<?> getOrdersByCustomer(Authentication authentication) {
        try {
            logger.info("Fetching orders for customer");
            UUID customerId = UUID.fromString(authentication.getName());
            List<OrderResponseDTO> orders = orderService.getOrdersByCustomer(customerId);

            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(true)
                            .message("Customer orders fetched successfully")
                            .data(orders)
                            .count(orders.size())
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Customer orders fetch failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Customer orders fetch error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/rider")
    @PreAuthorize("hasRole('RIDER')")
    @Cacheable(value = "orders", key = "'rider_orders_' + #riderId")
    public ResponseEntity<?> getOrdersByRider(Authentication authentication) {
        try {
            logger.info("Fetching orders for rider");
            UUID riderId = UUID.fromString(authentication.getName());
            List<OrderResponseDTO> orders = orderService.getOrdersByRider(riderId);

            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(true)
                            .message("Rider orders fetched successfully")
                            .data(orders)
                            .count(orders.size())
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Rider orders fetch failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Rider orders fetch error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @GetMapping("/restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    @Cacheable(value = "orders", key = "'restaurant_orders_' + #restaurantId")
    public ResponseEntity<?> getOrdersByRestaurant(Authentication authentication) {
        try {
            logger.info("Fetching orders for restaurant");
            UUID restaurantId = UUID.fromString(authentication.getName());
            List<OrderResponseDTO> orders = orderService.getOrdersByRestaurant(restaurantId);

            return ResponseEntity.ok(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(true)
                            .message("Restaurant orders fetched successfully")
                            .data(orders)
                            .count(orders.size())
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Restaurant orders fetch failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Restaurant orders fetch error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<OrderResponseDTO>>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }

    @PostMapping("/{orderId}/assign-rider/{riderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    public ResponseEntity<?> assignRider(
            @PathVariable UUID orderId,
            @PathVariable UUID riderId) {
        try {
            logger.info("Assigning rider: {} to order: {}", riderId, orderId);
            OrderResponseDTO order = orderService.assignRider(orderId, riderId);

            return ResponseEntity.ok(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(true)
                            .message("Rider assigned successfully")
                            .data(order)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Rider assignment failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            logger.error("Rider assignment error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<OrderResponseDTO>builder()
                            .success(false)
                            .message("Internal server error")
                            .build()
            );
        }
    }
}
