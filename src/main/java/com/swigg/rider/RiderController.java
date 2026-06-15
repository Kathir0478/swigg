package com.swigg.rider;

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
@RequestMapping("/api/riders")
public class RiderController {

    private static final Logger logger = LoggerFactory.getLogger(RiderController.class);

    @Autowired
    private RiderService riderService;

    @Autowired
    private RiderRepository riderRepository;

    @PostMapping("/register/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<RiderInitResponseDTO>> registerRequest(
            Authentication authentication,
            @RequestBody RiderRegisterRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Rider registration request received for userId: {}", userId);
        try {
            RiderInitResponseDTO response = riderService.initiateRegister(userId, request);
            logger.info("Rider registration TOTP code generated for userId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider registration request failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider registration request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/register/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<RiderResponseDTO>> registerVerify(
            Authentication authentication,
            @RequestBody RiderRegisterVerifyRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Rider registration verification request received for userId: {}", userId);
        try {
            Rider rider = riderService.completeRegister(userId, request.getOtpCode());
            logger.info("Rider registration verified for userId: {}", userId);
            return ApiResponses.created("Rider registered and verified successfully", mapToResponseDTO(rider));
        } catch (IllegalArgumentException e) {
            logger.warn("Rider registration verification failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider registration verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<ApiResponse<RiderInitResponseDTO>> loginRequest(@RequestBody RiderLoginRequestDTO request) {
        logger.info("Rider login request received for username: {}", request.getUsername());
        try {
            RiderInitResponseDTO response = riderService.initiateLogin(request.getUsername(), request.getPassword());
            logger.info("Rider login TOTP code generated for username: {}", request.getUsername());
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider login request failed for username: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider login request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> loginVerify(@RequestBody RiderLoginVerifyRequestDTO request) {
        logger.info("Rider login verification request received for phone: {}", request.getPhoneNumber());
        try {
            Rider tempRider = riderRepository.findByUser_PhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Rider not found"));
            TokenResponseDTO response = riderService.completeLogin(tempRider.getRiderId(), request.getOtpCode());
            logger.info("Rider login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ApiResponses.ok("Login successful", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Rider login verification failed for phone: {}. Reason: {}", request.getPhoneNumber(), e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider login verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/delete/request")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Rider delete OTP request received for riderId: {}", userId);
        try {
            riderService.requestDeletion(userId);
            logger.info("Rider delete OTP sent for riderId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number");
        } catch (IllegalArgumentException e) {
            logger.warn("Rider delete OTP request failed for riderId: {}. Reason: {}", userId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider delete OTP request error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/delete/complete")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<ApiResponse<Void>> deleteComplete(
            Authentication authentication,
            @RequestBody RiderDeleteVerifyDTO request) {
        UUID riderId = UUID.fromString(authentication.getName());
        logger.info("Rider delete completion request received for riderId: {}", riderId);
        try {
            riderService.completeDelete(riderId, request);
            logger.info("Rider deleted successfully for riderId: {}", riderId);
            return ApiResponses.ok("Rider account deactivated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Rider deletion failed for riderId: {}. Reason: {}", riderId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider deletion error", e);
            return ApiResponses.internalError();
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<ApiResponse<RiderResponseDTO>> updateRider(
            Authentication authentication,
            @RequestBody RiderUpdateRequestDTO request) {
        UUID riderId = UUID.fromString(authentication.getName());
        logger.info("Rider update request received for riderId: {}", riderId);
        try {
            Rider updatedRider = riderService.updateRider(riderId, request);
            logger.info("Rider updated successfully for riderId: {}", riderId);
            return ApiResponses.ok("Rider updated successfully", mapToResponseDTO(updatedRider));
        } catch (IllegalArgumentException e) {
            logger.warn("Rider update failed for riderId: {}. Reason: {}", riderId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider update error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<RiderResponseDTO>>> listRiders() {
        logger.info("List riders request received");
        try {
            List<Rider> riders = riderService.listAllRiders();
            List<RiderResponseDTO> riderDTOs = riders.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
            logger.info("Successfully fetched {} riders", riders.size());
            return ApiResponses.ok("Riders fetched successfully", riderDTOs, riderDTOs.size());
        } catch (Exception e) {
            logger.error("List riders error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/{riderId}")
    public ResponseEntity<ApiResponse<RiderResponseDTO>> getRider(@PathVariable UUID riderId) {
        logger.info("Get rider request received for riderId: {}", riderId);
        try {
            Rider rider = riderService.getRiderById(riderId);
            logger.info("Successfully fetched rider for riderId: {}", riderId);
            return ApiResponses.ok("Rider fetched successfully", mapToResponseDTO(rider));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to fetch rider for riderId: {}. Reason: {}", riderId, e.getMessage());
            return ApiResponses.notFound(e.getMessage());
        } catch (Exception e) {
            logger.error("Rider fetch error", e);
            return ApiResponses.internalError();
        }
    }

    private RiderResponseDTO mapToResponseDTO(Rider rider) {
        return RiderResponseDTO.builder()
                .riderId(rider.getRiderId())
                .userId(rider.getUser().getUserId())
                .name(rider.getName())
                .address(rider.getAddress())
                .dob(rider.getDob())
                .gender(rider.getGender())
                .lat(rider.getLat())
                .lng(rider.getLng())
                .vehicleNumber(rider.getVehicleNumber())
                .dlNumber(rider.getDlNumber())
                .isActive(rider.getIsActive())
                .isVerified(rider.getIsVerified())
                .createdAt(rider.getCreatedAt())
                .updatedAt(rider.getUpdatedAt())
                .build();
    }
}
