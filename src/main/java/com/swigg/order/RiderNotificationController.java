package com.swigg.order;

import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
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
@RequestMapping("/api/rider-notifications")
public class RiderNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(RiderNotificationController.class);

    @Autowired
    private RiderNotificationService riderNotificationService;

    @PostMapping("/{notificationId}/accept")
    @CacheEvict(value = "riderNotifications", allEntries = true)
    public ResponseEntity<ApiResponse<RiderNotificationResponseDTO>> acceptNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("X-Rider-Id") UUID riderId) {
        logger.info("POST /api/rider-notifications/{}/accept - Rider: {} accepting notification", notificationId, riderId);
        try {
            RiderNotificationResponseDTO response = riderNotificationService.acceptNotification(notificationId, riderId);
            return ResponseEntity.ok(ApiResponse.success(response, "Notification accepted successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error accepting notification: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{notificationId}/decline")
    @CacheEvict(value = "riderNotifications", allEntries = true)
    public ResponseEntity<ApiResponse<RiderNotificationResponseDTO>> declineNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("X-Rider-Id") UUID riderId) {
        logger.info("POST /api/rider-notifications/{}/decline - Rider: {} declining notification", notificationId, riderId);
        try {
            RiderNotificationResponseDTO response = riderNotificationService.declineNotification(notificationId, riderId);
            return ResponseEntity.ok(ApiResponse.success(response, "Notification declined successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error declining notification: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rider/{riderId}/pending")
    public ResponseEntity<ApiResponses<RiderNotificationResponseDTO>> getPendingNotificationsForRider(
            @PathVariable UUID riderId) {
        logger.info("GET /api/rider-notifications/rider/{}/pending - Fetching pending notifications", riderId);
        try {
            List<RiderNotificationResponseDTO> responses = riderNotificationService.getPendingNotificationsForRider(riderId);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching pending notifications: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<ApiResponses<RiderNotificationResponseDTO>> getNotificationsForCart(
            @PathVariable UUID cartId) {
        logger.info("GET /api/rider-notifications/cart/{} - Fetching notifications for cart", cartId);
        try {
            List<RiderNotificationResponseDTO> responses = riderNotificationService.getNotificationsForCart(cartId);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching notifications for cart: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }

    @PostMapping("/cart/{cartId}/send")
    @CacheEvict(value = "riderNotifications", allEntries = true)
    public ResponseEntity<ApiResponse<Void>> sendNotificationsToNearbyRiders(@PathVariable UUID cartId) {
        logger.info("POST /api/rider-notifications/cart/{}/send - Sending notifications to nearby riders", cartId);
        try {
            riderNotificationService.sendNotificationsToNearbyRiders(cartId);
            return ResponseEntity.ok(ApiResponse.success(null, "Notifications sent successfully to nearby riders"));
        } catch (IllegalArgumentException e) {
            logger.error("Error sending notifications: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
