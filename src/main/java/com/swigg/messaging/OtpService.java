package com.swigg.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private SmsService smsService;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    public void sendOtp(String phoneNumber, OtpPurpose purpose) {
        String code = generateCode();
        String key = buildKey(phoneNumber, purpose);

        otpStore.put(key, new OtpEntry(code, LocalDateTime.now().plusMinutes(expirationMinutes)));
        logger.info("Generated OTP for purpose {} on phone ending {}", purpose, lastFourDigits(phoneNumber));

        String message = String.format("Your Swigg verification code is %s. Valid for %d minutes.", code, expirationMinutes);
        smsService.sendSms(phoneNumber, message);
        logger.info("OTP sent for purpose {} to phone ending {}", purpose, lastFourDigits(phoneNumber));
    }

    public boolean verifyOtp(String phoneNumber, String code, OtpPurpose purpose) {
        if (code == null || code.isBlank()) {
            logger.warn("OTP verification failed: code is empty for purpose {}", purpose);
            return false;
        }

        String key = buildKey(phoneNumber, purpose);
        OtpEntry entry = otpStore.get(key);

        if (entry == null) {
            logger.warn("OTP verification failed: no OTP found for purpose {} on phone ending {}",
                    purpose, lastFourDigits(phoneNumber));
            return false;
        }

        if (entry.expiresAt().isBefore(LocalDateTime.now())) {
            otpStore.remove(key);
            logger.warn("OTP verification failed: OTP expired for purpose {} on phone ending {}",
                    purpose, lastFourDigits(phoneNumber));
            return false;
        }

        if (!entry.code().equals(code.trim())) {
            logger.warn("OTP verification failed: invalid code for purpose {} on phone ending {}",
                    purpose, lastFourDigits(phoneNumber));
            return false;
        }

        otpStore.remove(key);
        logger.info("OTP verified successfully for purpose {} on phone ending {}", purpose, lastFourDigits(phoneNumber));
        return true;
    }

    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, otpLength);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + otpLength + "d", code);
    }

    private String buildKey(String phoneNumber, OtpPurpose purpose) {
        return purpose.name() + ":" + phoneNumber;
    }

    private String lastFourDigits(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }

    private record OtpEntry(String code, LocalDateTime expiresAt) {}
}
