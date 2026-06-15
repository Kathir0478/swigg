package com.swigg.order;

import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody OrderRequestDTO request) {
        logger.info("POST /api/orders - Creating order for customer: {}", customerId);
        try {
            OrderResponseDTO response = orderService.createOrder(customerId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Order created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/assign-rider/{riderId}")
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<ApiResponse<OrderResponseDTO>> assignRider(
            @PathVariable UUID orderId,
            @PathVariable UUID riderId) {
        logger.info("POST /api/orders/{}/assign-rider/{} - Assigning rider to order", orderId, riderId);
        try {
            OrderResponseDTO response = orderService.assignRider(orderId, riderId);
            return ResponseEntity.ok(ApiResponse.success(response, "Rider assigned successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error assigning rider: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/status")
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {
        logger.info("PUT /api/orders/{}/status - Updating order status to: {}", orderId, status);
        try {
            OrderResponseDTO response = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(ApiResponse.success(response, "Order status updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-Customer-Id") UUID customerId) {
        logger.info("DELETE /api/orders/{} - Cancelling order by customer: {}", orderId, customerId);
        try {
            orderService.cancelOrder(orderId, customerId);
            return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable UUID orderId) {
        logger.info("GET /api/orders/{} - Fetching order", orderId);
        try {
            OrderResponseDTO response = orderService.getOrderById(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponses<OrderResponseDTO>> getOrdersByCustomer(@PathVariable UUID customerId) {
        logger.info("GET /api/orders/customer/{} - Fetching orders for customer", customerId);
        try {
            List<OrderResponseDTO> responses = orderService.getOrdersByCustomer(customerId);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching orders for customer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponses<OrderResponseDTO>> getOrdersByRider(@PathVariable UUID riderId) {
        logger.info("GET /api/orders/rider/{} - Fetching orders for rider", riderId);
        try {
            List<OrderResponseDTO> responses = orderService.getOrdersByRider(riderId);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching orders for rider: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }
}
