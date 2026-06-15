package com.swigg.user;

import com.swigg.auth.AuthService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @PostMapping("/signup/request")
    public ResponseEntity<ApiResponse<SignupInitResponseDTO>> signup(@RequestBody SignupRequestDTO request) {
        logger.info("Signup request received for username: {}", request.getUsername());
        try {
            SignupInitResponseDTO response = userService.initSignup(request);
            logger.info("Signup OTP sent for username: {}", request.getUsername());
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Signup initialization failed for username: {}. Reason: {}",
                    request.getUsername(), e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Signup request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> verifySignup(@RequestBody SignupVerifyRequestDTO request) {
        logger.info("Signup verification request received for phone: {}", request.getPhoneNumber());
        try {
            User user = userService.completeSignup(request);
            TokenResponseDTO tokens = authService.generateTokensForUser(user);
            logger.info("Signup completed and tokens issued for phone: {}", request.getPhoneNumber());
            return ApiResponses.created("Signup completed successfully", tokens);
        } catch (IllegalArgumentException e) {
            logger.warn("Signup verification failed for phone: {}. Reason: {}",
                    request.getPhoneNumber(), e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Signup verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<ApiResponse<LoginInitResponseDTO>> loginRequest(@RequestBody LoginRequestDTO request) {
        logger.info("Login request received for username: {}", request.getUsername());
        try {
            LoginInitResponseDTO response = userService.initiateLogin(request);
            logger.info("Login TOTP code generated for username: {}", request.getUsername());
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Login request failed for username: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Login request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> verifyLogin(@RequestBody LoginVerifyRequestDTO request) {
        logger.info("Login verification request received for phone: {}", request.getPhoneNumber());
        try {
            TokenResponseDTO response = userService.completeLogin(request);
            logger.info("Login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ApiResponses.ok("Login successful", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Login verification failed for phone: {}. Reason: {}", request.getPhoneNumber(), e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Login verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/delete/request")
    public ResponseEntity<ApiResponse<Void>> requestDeletion(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Delete OTP request received for userId: {}", userId);
        try {
            userService.requestDeletion(userId);
            logger.info("Delete OTP sent for userId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number");
        } catch (IllegalArgumentException e) {
            logger.warn("Delete OTP request failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Delete OTP request error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/delete/complete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(Authentication authentication,
                                                           @RequestBody DeleteUserRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Delete account request received for userId: {}", userId);
        try {
            userService.deleteUser(userId, request.getOtpCode());
            logger.info("Account deleted successfully for userId: {}", userId);
            return ApiResponses.ok("Account deactivated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Account deletion failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Account deletion error", e);
            return ApiResponses.internalError();
        }
    }
}
