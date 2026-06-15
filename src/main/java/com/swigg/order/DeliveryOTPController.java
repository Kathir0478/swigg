package com.swigg.order;

import com.swigg.common.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/delivery-otp")
public class DeliveryOTPController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryOTPController.class);

    @Autowired
    private DeliveryOTPService deliveryOTPService;

    @PostMapping("/{orderId}/generate")
    public ResponseEntity<ApiResponse<String>> generateDeliveryOTP(@PathVariable UUID orderId) {
        logger.info("POST /api/delivery-otp/{}/generate - Generating delivery OTP", orderId);
        try {
            String otpCode = deliveryOTPService.generateDeliveryOTP(orderId);
            return ResponseEntity.ok(ApiResponse.success(otpCode, "Delivery OTP generated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error generating delivery OTP: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyDeliveryOTP(
            @PathVariable UUID orderId,
            @RequestParam @NotBlank String otpCode) {
        logger.info("POST /api/delivery-otp/{}/verify - Verifying delivery OTP", orderId);
        try {
            boolean verified = deliveryOTPService.verifyDeliveryOTP(orderId, otpCode);
            return ResponseEntity.ok(ApiResponse.success(verified, "Delivery OTP verified successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error verifying delivery OTP: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
