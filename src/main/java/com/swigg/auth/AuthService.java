package com.swigg.auth;

import com.swigg.messaging.OtpPurpose;
import com.swigg.messaging.OtpSentResponseDTO;
import com.swigg.messaging.OtpService;
import com.swigg.user.User;
import com.swigg.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    public OtpSentResponseDTO initiateLogin(TokenRequestDTO data) {
        String username = data.getUsername();
        logger.info("Login initiated for user: {}", username);

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    logger.warn("Login initiation failed: User '{}' not found", username);
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Login initiation failed: account deactivated for user '{}'", username);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!passwordEncoder.matches(data.getPassword(), user.getPasswordHash())) {
            logger.warn("Login initiation failed: Incorrect password for user '{}'", username);
            throw new IllegalArgumentException("Invalid username or password");
        }

        logger.info("Password verified for user: '{}'. Sending login OTP to mobile.", username);
        otpService.sendOtp(user.getPhoneNumber(), OtpPurpose.LOGIN);

        return new OtpSentResponseDTO(
                "OTP sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    public TokenResponseDTO verifyLogin(LoginVerifyRequestDTO data) {
        String username = data.getUsername();
        logger.info("Login OTP verification for user: {}", username);

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    logger.warn("Login verification failed: User '{}' not found", username);
                    return new IllegalArgumentException("Invalid username");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Login verification failed: account deactivated for user '{}'", username);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!otpService.verifyOtp(user.getPhoneNumber(), data.getOtpCode(), OtpPurpose.LOGIN)) {
            logger.warn("Login verification failed: invalid OTP for user '{}'", username);
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        logger.info("Mobile OTP verified for user: '{}'. Generating tokens...", username);
        return generateTokensForUser(user);
    }

    public TokenResponseDTO generateTokensForUser(User user) {
        String accessToken = jwtService.generateAccessToken(new AuthRequestDTO(user.getUserId(), user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(user.getUserId());
        logger.info("Tokens generated for userId: {}", user.getUserId());
        return new TokenResponseDTO(accessToken, refreshToken);
    }

    public TokenResponseDTO refreshTokens(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        logger.info("Refresh token request received");

        if (refreshToken == null || refreshToken.isBlank()) {
            logger.warn("Refresh failed: refresh token is required");
            throw new IllegalArgumentException("Refresh token is required");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            logger.warn("Refresh failed: token is not a valid refresh token");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            logger.warn("Refresh failed: refresh token expired");
            throw new IllegalArgumentException("Refresh token expired");
        }

        UUID userId = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Refresh failed: user '{}' not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Refresh failed: account deactivated for userId '{}'", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        logger.info("Refresh token valid for userId: {}. Issuing new tokens.", userId);
        return generateTokensForUser(user);
    }
}
