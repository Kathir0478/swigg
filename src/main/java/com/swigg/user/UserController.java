package com.swigg.user;

import com.swigg.auth.AuthService;
import com.swigg.auth.TokenResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO request) {
        logger.info("Signup request received for username: {}", request.getUsername());
        try {
            SignupInitResponseDTO response = userService.initSignup(request);
            logger.info("Signup OTP sent for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Signup initialization failed for username: {}. Reason: {}",
                    request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifySignup(@RequestBody SignupVerifyRequestDTO request) {
        logger.info("Signup verification request received for phone: {}", request.getPhoneNumber());
        try {
            User user = userService.completeSignup(request);
            TokenResponseDTO tokens = authService.generateTokensForUser(user);
            logger.info("Signup completed and tokens issued for phone: {}", request.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
        } catch (IllegalArgumentException e) {
            logger.warn("Signup verification failed for phone: {}. Reason: {}",
                    request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<?> loginRequest(@RequestBody LoginRequestDTO request) {
        logger.info("Login request received for username: {}", request.getUsername());
        try {
            LoginInitResponseDTO response = userService.initiateLogin(request);
            logger.info("Login TOTP code generated for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Login request failed for username: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verifyLogin(@RequestBody LoginVerifyRequestDTO request) {
        logger.info("Login verification request received for phone: {}", request.getPhoneNumber());
        try {
            TokenResponseDTO response = userService.completeLogin(request);
            logger.info("Login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Login verification failed for phone: {}. Reason: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/delete/request")
    public ResponseEntity<?> requestDeletion(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Delete OTP request received for userId: {}", userId);
        try {
            var response = userService.requestDeletion(userId);
            logger.info("Delete OTP sent for userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Delete OTP request failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/complete")
    public ResponseEntity<?> deleteAccount(Authentication authentication,
                                           @RequestBody DeleteUserRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Delete account request received for userId: {}", userId);
        try {
            userService.deleteUser(userId, request.getOtpCode());
            logger.info("Account deleted successfully for userId: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Account deactivated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Account deletion failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
