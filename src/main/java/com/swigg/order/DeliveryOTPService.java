package com.swigg.order;

import com.swigg.auth.TotpService;
import com.swigg.messaging.SmsService;
import com.swigg.order.Order;
import com.swigg.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class DeliveryOTPService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryOTPService.class);

    @Autowired
    private DeliveryOTPRepository deliveryOTPRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SmsService smsService;

    @Value("${delivery.otp.length:6}")
    private int otpLength;

    @Value("${delivery.otp.expiration.minutes:15}")
    private int otpExpirationMinutes;

    @Value("${delivery.otp.sms.provider:logging}")
    private String deliveryOtpSmsProvider;

    @Transactional
    public String generateDeliveryOTP(UUID orderId) {
        logger.info("Generating delivery OTP for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new IllegalArgumentException("Order not found");
                });

        if (!order.getStatus().equals(OrderStatus.IN_TRANSIT)) {
            logger.warn("Order is not in IN_TRANSIT status: {}", order.getStatus());
            throw new IllegalArgumentException("Order must be in IN_TRANSIT status to generate delivery OTP");
        }

        Optional<DeliveryOTP> existingOTP = deliveryOTPRepository.findByOrderIdAndIsVerified(orderId, false);
        if (existingOTP.isPresent() && existingOTP.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            logger.info("Valid OTP already exists for order: {}", orderId);
            return existingOTP.get().getCode();
        }

        String otpCode = generateOTPCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        DeliveryOTP deliveryOTP = DeliveryOTP.builder()
                .order(order)
                .orderId(orderId)
                .code(otpCode)
                .isVerified(false)
                .expiresAt(expiresAt)
                .build();

        DeliveryOTP savedOTP = deliveryOTPRepository.save(deliveryOTP);
        logger.info("Delivery OTP generated successfully for order: {}", orderId);

        sendDeliveryOTP(order.getCustomerId(), otpCode);

        return savedOTP.getCode();
    }

    @Transactional
    public boolean verifyDeliveryOTP(UUID orderId, String otpCode) {
        logger.info("Verifying delivery OTP for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new IllegalArgumentException("Order not found");
                });

        DeliveryOTP deliveryOTP = deliveryOTPRepository.findValidOTPByOrderIdAndCode(orderId, otpCode, LocalDateTime.now())
                .orElseThrow(() -> {
                    logger.warn("Invalid or expired OTP for order: {}", orderId);
                    return new IllegalArgumentException("Invalid or expired OTP");
                });

        if (deliveryOTP.getIsVerified()) {
            logger.warn("OTP already verified for order: {}", orderId);
            throw new IllegalArgumentException("OTP already verified");
        }

        deliveryOTP.setIsVerified(true);
        deliveryOTP.setVerifiedAt(LocalDateTime.now());
        deliveryOTPRepository.save(deliveryOTP);

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        logger.info("Delivery OTP verified successfully for order: {}", orderId);
        return true;
    }

    private String generateOTPCode() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private void sendDeliveryOTP(UUID customerId, String otpCode) {
        try {
            String message = String.format("Your delivery OTP is: %s. Please share this with the rider upon delivery. Valid for %d minutes.", 
                    otpCode, otpExpirationMinutes);
            smsService.sendSms(customerId.toString(), message);
            logger.info("Delivery OTP sent to customer: {}", customerId);
        } catch (Exception e) {
            logger.error("Failed to send delivery OTP to customer: {}", customerId, e);
        }
    }
}
