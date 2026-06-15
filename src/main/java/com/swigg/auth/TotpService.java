package com.swigg.auth;

import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class TotpService {

    private static final Logger logger = LoggerFactory.getLogger(TotpService.class);
    private static final String ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP = 30;
    private static final int TOTP_LENGTH = 6;
    private static final int SECRET_LENGTH = 32;
    private static final int VERIFICATION_WINDOW = 1;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] randomBytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(randomBytes);
        Base32 base32 = new Base32();
        String secret = base32.encodeToString(randomBytes).replaceAll("=", "");
        logger.debug("Generated new TOTP secret");
        return secret;
    }

    public String generateTotp(String secret, long timestamp) {
        try {
            long timeCounter = timestamp / 1000 / TIME_STEP;
            return computeTotp(secret, timeCounter);
        } catch (Exception e) {
            logger.error("Error generating TOTP code", e);
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    public boolean verifyTotp(String secret, String code, long timestamp) {
        return verifyTotp(secret, code, timestamp, VERIFICATION_WINDOW);
    }

    public boolean verifyTotp(String secret, String code, long timestamp, int windowSize) {
        try {
            if (code == null || code.isBlank()) {
                logger.warn("TOTP verification failed: code is empty");
                return false;
            }

            long timeCounter = timestamp / 1000 / TIME_STEP;

            for (int i = -windowSize; i <= windowSize; i++) {
                String expectedCode = computeTotp(secret, timeCounter + i);
                if (expectedCode.equals(code.trim())) {
                    logger.debug("TOTP verified successfully within window");
                    return true;
                }
            }

            logger.warn("TOTP verification failed: invalid code");
            return false;
        } catch (Exception e) {
            logger.error("Error verifying TOTP code", e);
            return false;
        }
    }

    private String computeTotp(String secret, long timeCounter)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Base32 base32 = new Base32();
        byte[] secretBytes = base32.decode(secret);

        byte[] message = new byte[8];
        for (int i = 7; i >= 0; i--) {
            message[i] = (byte) (timeCounter & 0xff);
            timeCounter >>= 8;
        }

        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretBytes, 0, secretBytes.length, ALGORITHM);
        mac.init(keySpec);

        byte[] hash = mac.doFinal(message);
        int offset = hash[hash.length - 1] & 0xf;

        int truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= hash[offset + i] & 0xff;
        }

        truncatedHash &= 0x7fffffff;
        truncatedHash %= (int) Math.pow(10, TOTP_LENGTH);

        return String.format("%0" + TOTP_LENGTH + "d", truncatedHash);
    }
}
