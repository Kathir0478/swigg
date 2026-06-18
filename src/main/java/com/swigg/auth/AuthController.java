package com.swigg.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDTO request) {
        logger.info("Token refresh request received");
        try {
            TokenResponseDTO response = authService.refreshTokens(request);
            logger.info("Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Token refresh failed. Reason: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        logger.info("User profile fetch request received");
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("User profile fetch failed: missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new UserProfileResponseDTO(false, "Invalid or expired access token", null));
            }

            String token = authHeader.substring(7);
            String userIdStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            UUID userId = UUID.fromString(userIdStr);

            UserProfileResponseDTO response = authService.getUserProfile(userId, token);
            logger.info("User profile fetch successful");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("User profile fetch failed. Reason: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new UserProfileResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("User profile fetch failed with error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UserProfileResponseDTO(false, "Internal server error", null));
        }
    }
}
