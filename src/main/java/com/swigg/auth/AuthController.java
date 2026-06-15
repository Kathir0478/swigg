package com.swigg.auth;

import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refresh(@RequestBody RefreshTokenRequestDTO request) {
        logger.info("Token refresh request received");
        try {
            TokenResponseDTO response = authService.refreshTokens(request);
            logger.info("Token refresh successful");
            return ApiResponses.ok("Token refresh successful", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Token refresh failed. Reason: {}", e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return ApiResponses.internalError();
        }
    }
}
