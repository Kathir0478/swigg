package com.swigg.user;

import com.swigg.auth.AuthRequestDTO;
import com.swigg.auth.AuthService;
import com.swigg.auth.TotpService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.messaging.OtpSentResponseDTO;
import com.swigg.messaging.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private TotpService totpService;

    @Autowired
    private AuthService authService;

    @Transactional
    public SignupInitResponseDTO initSignup(SignupRequestDTO request) {
        String username = request.getUsername();
        String phoneNumber = request.getPhoneNumber();
        String password = request.getPassword();

        logger.info("Signup initiated for username: {}", username);

        if (username == null || username.isBlank()) {
            logger.warn("Signup failed: username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            logger.warn("Signup failed for username '{}': phone number is required", username);
            throw new IllegalArgumentException("Phone number is required");
        }
        if (password == null || password.isBlank()) {
            logger.warn("Signup failed for username '{}': password is required", username);
            throw new IllegalArgumentException("Password is required");
        }
        User existingUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        String totpSecret;
        if (existingUser==null){
            totpSecret = totpService.generateSecret();
            String passwordHash = passwordEncoder.encode(password);
            User user = User.builder()
                    .userName(username)
                    .phoneNumber(phoneNumber)
                    .passwordHash(passwordHash)
                    .role(Role.USER)
                    .isActive(true)
                    .isVerified(false)
                    .totpSecret(totpSecret)
                    .build();

            User savedUser = userRepository.save(user);
            logger.info("User created in database for username: {} with isVerified=false", username);
        }
        else {
            totpSecret=existingUser.getTotpSecret();
        }

        String totpCode = totpService.generateTotp(totpSecret, System.currentTimeMillis());
        logger.warn("SIGNUP TOTP CODE FOR PHONE {}: {}", phoneNumber, totpCode);

        String maskedPhone = OtpService.maskPhoneNumber(phoneNumber);
        logger.info("Signup verification code generated for username: {}. Awaiting verification.", username);

        return new SignupInitResponseDTO(
                username,
                "Verification code sent to your mobile number",
                maskedPhone
        );
    }

    @Transactional
    public User completeSignup(SignupVerifyRequestDTO request) {
        String phoneNumber = request.getPhoneNumber();
        String totpCode = request.getOtpCode();
        logger.info("Signup verification started for phone: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> {
                    logger.warn("Signup verification failed: no user found for phone: {}", phoneNumber);
                    return new IllegalArgumentException("User not found. Please signup first.");
                });

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            logger.warn("Signup verification failed: user already verified for phone: {}", phoneNumber);
            throw new IllegalArgumentException("User is already verified");
        }

        logger.info("Verifying TOTP code for phone: {}", phoneNumber);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Signup verification failed: invalid TOTP code for phone: {}", phoneNumber);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        user.setIsVerified(true);
        User updatedUser = userRepository.save(user);
        logger.info("User verified successfully for username: {} with isVerified=true", user.getUserName());

        return updatedUser;
    }

    public OtpSentResponseDTO requestDeletion(UUID userId) {
        logger.info("Account deletion OTP requested for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion OTP request failed: user '{}' not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Deletion OTP request failed: user '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        String totpCode = totpService.generateTotp(user.getTotpSecret(), System.currentTimeMillis());
        logger.warn("DELETE TOTP CODE FOR PHONE {}: {}", user.getPhoneNumber(), totpCode);

        return new OtpSentResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    @Transactional
    public void deleteUser(UUID userId, String otpCode) {
        logger.info("Account deletion requested for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion failed: user '{}' not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Deletion failed: user '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        logger.info("Verifying TOTP code for account deletion: userId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), otpCode, System.currentTimeMillis())) {
            logger.warn("Deletion failed: invalid TOTP code for userId '{}'", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        user.setIsActive(false);
        userRepository.save(user);
        logger.info("Account deactivated successfully for userId: {}", userId);
    }

    public LoginInitResponseDTO initiateLogin(LoginRequestDTO request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String phoneNumber= request.getPhoneNumber();

        logger.info("Login initiated for username: {}", username);

        if (username == null || username.isBlank()) {
            logger.warn("Login failed: username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            logger.warn("Login failed: password is required");
            throw new IllegalArgumentException("Password is required");
        }

        if (phoneNumber==null || phoneNumber.isBlank()){
            logger.warn("Phone number is required");
            throw new IllegalArgumentException("Phone number  is required");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user '{}' not found", username);
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Login failed: account deactivated for user '{}'", username);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            logger.warn("Login failed: account not verified for user '{}'", username);
            throw new IllegalArgumentException("Account is not verified");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Login failed: incorrect password for user '{}'", username);
            throw new IllegalArgumentException("Invalid username or password");
        }

        String totpCode = totpService.generateTotp(user.getTotpSecret(), System.currentTimeMillis());
        logger.warn("LOGIN TOTP CODE FOR PHONE {}: {}", user.getPhoneNumber(), totpCode);

        logger.info("Login TOTP code generated for username: {}. Awaiting verification.", username);
        return new LoginInitResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    public TokenResponseDTO completeLogin(LoginVerifyRequestDTO request) {
        String phoneNumber = request.getPhoneNumber();
        String totpCode = request.getOtpCode();

        logger.info("Login verification started for phone: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> {
                    logger.warn("Login verification failed: no user found for phone: {}", phoneNumber);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Login verification failed: account deactivated for phone: {}", phoneNumber);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            logger.warn("Login verification failed: account not verified for phone: {}", phoneNumber);
            throw new IllegalArgumentException("Account is not verified");
        }

        logger.info("Verifying TOTP code for login: {}", phoneNumber);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Login verification failed: invalid TOTP code for phone: {}", phoneNumber);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        logger.info("Login successful for username: {}", user.getUserName());
        return authService.generateTokensForUser(user);
    }
}
