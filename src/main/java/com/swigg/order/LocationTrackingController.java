package com.swigg.order;

import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/location-tracking")
public class LocationTrackingController {

    private static final Logger logger = LoggerFactory.getLogger(LocationTrackingController.class);

    @Autowired
    private LocationTrackingService locationTrackingService;

    @PostMapping
    @CacheEvict(value = "locationTracking", allEntries = true)
    public ResponseEntity<ApiResponse<LocationTrackingResponseDTO>> updateLocation(
            @Valid @RequestBody LocationTrackingRequestDTO request) {
        logger.info("POST /api/location-tracking - Updating location for order: {} rider: {}", request.getOrderId(), request.getRiderId());
        try {
            LocationTrackingResponseDTO response = locationTrackingService.updateLocation(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Location updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error updating location: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponses<LocationTrackingResponseDTO>> getLocationsByOrder(@PathVariable UUID orderId) {
        logger.info("GET /api/location-tracking/order/{} - Fetching location history", orderId);
        try {
            List<LocationTrackingResponseDTO> responses = locationTrackingService.getLocationsByOrder(orderId);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching location history: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}/latest")
    public ResponseEntity<ApiResponse<LocationTrackingResponseDTO>> getLatestLocationByOrder(@PathVariable UUID orderId) {
        logger.info("GET /api/location-tracking/order/{}/latest - Fetching latest location", orderId);
        try {
            LocationTrackingResponseDTO response = locationTrackingService.getLatestLocationByOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching latest location: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}/since")
    public ResponseEntity<ApiResponses<LocationTrackingResponseDTO>> getLocationsByOrderSince(
            @PathVariable UUID orderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        logger.info("GET /api/location-tracking/order/{}/since - Fetching location history since: {}", orderId, since);
        try {
            List<LocationTrackingResponseDTO> responses = locationTrackingService.getLocationsByOrderSince(orderId, since);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching location history since: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponses<LocationTrackingResponseDTO>> getLocationsByRider(@PathVariable UUID riderId) {
        logger.info("GET /api/location-tracking/rider/{} - Fetching location history for rider", riderId);
        try {
            List<LocationTrackingResponseDTO> responses = locationTrackingService.getLocationsByRider(riderId);
            return ResponseEntity.ok(ApiResponses.success(responses));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching location history for rider: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponses.error(e.getMessage()));
        }
    }
}
