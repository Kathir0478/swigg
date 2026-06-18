package com.swigg.auth;

import com.swigg.messaging.SmsService;
import com.swigg.user.User;
import com.swigg.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AsyncOtpService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncOtpService.class);

    @Autowired
    private TotpService totpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmsService smsService;

    @Async
    public void generateAndSendOtpAsync(String userId, String phoneNumber, String type) {
        try {
            logger.info("Generating OTP asynchronously for userId: {} with type: {}", userId, type);

            User user = userRepository.findByUserId(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
            String secret = user.getTotpSecret();
            if (secret == null || secret.isBlank()) {
                secret = totpService.generateSecret();
                user.setTotpSecret(secret);
                userRepository.save(user);
            }
            String otpCode = totpService.generateTotp(secret, System.currentTimeMillis());

            logger.warn("OTP CODE FOR PHONE {}: {} [{}]", phoneNumber, otpCode, type);

            String message = String.format("Your Swigg verification code is %s.", otpCode);
            smsService.sendSms(phoneNumber, message);
            logger.info("OTP sent asynchronously for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Error in async OTP generation for userId: {}. Error: {}", userId, e.getMessage(), e);
        }
    }
}
