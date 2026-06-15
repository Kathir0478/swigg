package com.swigg.rider;

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
@RequestMapping("/api/riders")
public class RiderController {

    private static final Logger logger = LoggerFactory.getLogger(RiderController.class);

    @Autowired
    private RiderService riderService;

    @Autowired
    private RiderRepository riderRepository;

    @PostMapping("/register/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerRequest(Authentication authentication, @RequestBody RiderRegisterRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Rider registration request received for userId: {}", userId);
        try {
            RiderInitResponseDTO response = riderService.initiateRegister(userId, request);
            logger.info("Rider registration TOTP code generated for userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider registration request failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerVerify(Authentication authentication, @RequestBody RiderRegisterVerifyRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Rider registration verification request received for userId: {}", userId);
        try {
            Rider rider = riderService.completeRegister(userId, request.getOtpCode());
            logger.info("Rider registration verified for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Rider registered and verified successfully",
                    "riderId", rider.getRiderId()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Rider registration verification failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<?> loginRequest(@RequestBody RiderLoginRequestDTO request) {
        logger.info("Rider login request received for username: {}", request.getUsername());
        try {
            RiderInitResponseDTO response = riderService.initiateLogin(request.getUsername(), request.getPassword());
            logger.info("Rider login TOTP code generated for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider login request failed for username: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@RequestBody RiderLoginVerifyRequestDTO request) {
        logger.info("Rider login verification request received for phone: {}", request.getPhoneNumber());
        try {
            Rider tempRider = riderRepository.findByUser_PhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Rider not found"));
            TokenResponseDTO response = riderService.completeLogin(tempRider.getRiderId(), request.getOtpCode());
            logger.info("Rider login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider login verification failed for phone: {}. Reason: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/delete/request")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<?> deleteRequest(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Rider delete OTP request received for riderId: {}", userId);
        try {
            var response = riderService.requestDeletion(userId);
            logger.info("Rider delete OTP sent for riderId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider delete OTP request failed for riderId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/complete")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<?> deleteComplete(Authentication authentication, @RequestBody RiderDeleteVerifyDTO request) {
        UUID riderId = UUID.fromString(authentication.getName());
        logger.info("Rider delete completion request received for riderId: {}", riderId);
        try {
            riderService.completeDelete(riderId, request);
            logger.info("Rider deleted successfully for riderId: {}", riderId);
            return ResponseEntity.ok(Map.of("message", "Rider account deactivated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Rider deletion failed for riderId: {}. Reason: {}", riderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<?> updateRider(Authentication authentication, @RequestBody RiderUpdateRequestDTO request) {
        UUID riderId = UUID.fromString(authentication.getName());
        logger.info("Rider update request received for riderId: {}", riderId);
        try {
            Rider updatedRider = riderService.updateRider(riderId, request);
            logger.info("Rider updated successfully for riderId: {}", riderId);
            return ResponseEntity.ok(Map.of(
                    "message", "Rider updated successfully",
                    "riderId", updatedRider.getRiderId()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Rider update failed for riderId: {}. Reason: {}", riderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listRiders() {
        logger.info("List riders request received");
        try {
            List<Rider> riders = riderService.listAllRiders();
            logger.info("Successfully fetched {} riders", riders.size());
            return ResponseEntity.ok(riders);
        } catch (Exception e) {
            logger.warn("Failed to list riders. Reason: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{riderId}")
    public ResponseEntity<?> getRider(@PathVariable UUID riderId) {
        logger.info("Get rider request received for riderId: {}", riderId);
        try {
            Rider rider = riderService.getRiderById(riderId);
            logger.info("Successfully fetched rider for riderId: {}", riderId);
            return ResponseEntity.ok(rider);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to fetch rider for riderId: {}. Reason: {}", riderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
