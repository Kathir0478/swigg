package com.swigg.rider;

import com.swigg.auth.AuthService;
import com.swigg.auth.AsyncOtpService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.auth.TotpService;
import com.swigg.geocoding.AsyncGeocodingService;
import com.swigg.geocoding.GeocodingService;
import com.swigg.messaging.OtpSentResponseDTO;
import com.swigg.messaging.OtpService;
import com.swigg.user.Role;
import com.swigg.user.User;
import com.swigg.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RiderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AsyncGeocodingService asyncGeocodingService;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    @Autowired
    private AsyncOtpService asyncOtpService;

    @Autowired
    private AuthService authService;

    @Transactional
    public RiderInitResponseDTO initiateRegister(UUID userId, RiderRegisterRequestDTO request) {
        logger.info("Rider registration initiated for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Rider registration failed: user '{}' not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Rider registration failed: account deactivated for user '{}'", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (riderRepository.findById(userId).isPresent()) {
            logger.warn("Rider registration failed: rider already exists for user '{}'", userId);
            throw new IllegalArgumentException("Rider already registered for this user");
        }

        if (request.getLat() == null || request.getLng() == null) {
            logger.warn("Rider registration failed: latitude and longitude are required");
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        if (!geocodingService.validateCoordinates(request.getLat(), request.getLng())) {
            logger.warn("Rider registration failed: invalid coordinates for user '{}'", userId);
            throw new IllegalArgumentException("Invalid latitude or longitude coordinates");
        }
        Rider existingRider = riderRepository.findByUserId(userId).orElse(null);
        if (existingRider == null) {
            Rider rider = Rider.builder()
                    .userId(userId)
                    .user(user)
                    .name(user.getUserName())
                    .dob(request.getDob())
                    .gender(request.getGender())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .vehicleNumber(request.getVehicleNumber())
                    .dlNumber(request.getDlNumber())
                    .isActive(true)
                    .isVerified(false)
                    .build();

            riderRepository.save(rider);
            asyncGeocodingService.scheduleAddressUpdate(request.getLat(), request.getLng(), userId, "RIDER");
            logger.info("Rider created in database for userId: {} with isVerified=false", userId);
        }

        asyncOtpService.generateAndSendOtpAsync(userId.toString(), user.getPhoneNumber(), "RIDER_REGISTER");
        logger.info("Rider registration OTP send initiated asynchronously for userId: {}", userId);

        String maskedPhone = OtpService.maskPhoneNumber(user.getPhoneNumber());
        logger.info("Rider registration verification code generation initiated for userId: {}. Awaiting verification.", userId);

        return new RiderInitResponseDTO(
                "Verification code sent to your mobile number",
                maskedPhone
        );
    }

    @Transactional
    public Rider completeRegister(UUID userId, String totpCode) {
        logger.info("Rider registration verification started for userId: {}", userId);

        Rider rider = riderRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Rider registration verification failed: rider not found for userId: {}", userId);
                    return new IllegalArgumentException("Rider not found. Please register first.");
                });

        if (Boolean.TRUE.equals(rider.getIsVerified())) {
            logger.warn("Rider registration verification failed: rider already verified for userId: {}", userId);
            throw new IllegalArgumentException("Rider is already verified");
        }

        User user = rider.getUser();

        logger.info("Verifying TOTP code for rider registration: userId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Rider registration verification failed: invalid TOTP code for userId: {}", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        rider.setIsVerified(true);
        user.setRole(Role.RIDER);
        userRepository.save(user);
        Rider updatedRider = riderRepository.save(rider);
        evictRiderCache(updatedRider.getRiderId());
        logger.info("Rider verified successfully for userId: {} with isVerified=true and role=RIDER", userId);

        return updatedRider;
    }

    public RiderInitResponseDTO initiateLogin(String username, String password) {
        logger.info("Rider login initiated for username: {}", username);

        if (username == null || username.isBlank()) {
            logger.warn("Rider login failed: username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            logger.warn("Rider login failed: password is required");
            throw new IllegalArgumentException("Password is required");
        }

        Rider rider = riderRepository.findByUser_UserName(username)
                .orElseThrow(() -> {
                    logger.warn("Rider login failed: rider '{}' not found", username);
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Rider login failed: account deactivated for rider '{}'", username);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(rider.getIsVerified())) {
            logger.warn("Rider login failed: account not verified for rider '{}'", username);
            throw new IllegalArgumentException("Rider is not verified");
        }

        User user = rider.getUser();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Rider login failed: incorrect password for rider '{}'", username);
            throw new IllegalArgumentException("Invalid username or password");
        }

        asyncOtpService.generateAndSendOtpAsync(user.getUserId().toString(), user.getPhoneNumber(), "RIDER_LOGIN");
        logger.info("Rider login OTP send initiated asynchronously for username: {}", username);

        logger.info("Rider login TOTP code generation initiated for username: {}. Awaiting verification.", username);
        return new RiderInitResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    public TokenResponseDTO completeLogin(UUID riderId, String totpCode) {
        logger.info("Rider login verification started for riderId: {}", riderId);

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider login verification failed: rider not found for riderId: {}", riderId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Rider login verification failed: account deactivated for riderId: {}", riderId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(rider.getIsVerified())) {
            logger.warn("Rider login verification failed: account not verified for riderId: {}", riderId);
            throw new IllegalArgumentException("Rider is not verified");
        }

        User user = rider.getUser();
        logger.info("Verifying TOTP code for rider login: riderId={}", riderId);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Rider login verification failed: invalid TOTP code for riderId: {}", riderId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        logger.info("Rider login successful for username: {}", user.getUserName());
        return authService.generateTokensForUser(user);
    }

    public OtpSentResponseDTO requestDeletion(UUID userId) {
        logger.info("Rider deletion OTP requested for riderId: {}", userId);

        Rider rider = riderRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion OTP request failed: rider '{}' not found", userId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Deletion OTP request failed: rider '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        User user = rider.getUser();
        asyncOtpService.generateAndSendOtpAsync(userId.toString(), user.getPhoneNumber(), "RIDER_DELETE");
        logger.info("Rider deletion OTP send initiated asynchronously for riderId: {}", userId);

        return new OtpSentResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    @Transactional
    public void completeDelete(UUID userId, RiderDeleteVerifyDTO request) {
        logger.info("Rider deletion requested for riderId: {}", userId);

        Rider rider = riderRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion failed: rider '{}' not found", userId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Deletion failed: rider '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        User user = rider.getUser();
        logger.info("Verifying TOTP code for rider deletion: riderId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), request.getOtpCode(), System.currentTimeMillis())) {
            logger.warn("Deletion failed: invalid TOTP code for riderId '{}'", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        rider.setIsActive(false);
        user.setRole(Role.USER);
        userRepository.save(user);
        riderRepository.save(rider);
        evictRiderCache(rider.getRiderId());
        logger.info("Rider deactivated successfully for riderId: {} and role changed back to USER", userId);
    }

    @Transactional
    public Rider updateRider(UUID riderId, RiderUpdateRequestDTO request) {
        logger.info("Rider update requested for riderId: {}", riderId);

        Rider rider = riderRepository.findByUserId(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider update failed: rider '{}' not found", riderId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Rider update failed: rider '{}' is deactivated", riderId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            rider.setName(request.getName());
        }
        if (request.getAddress() != null) {
            rider.setAddress(request.getAddress());
        }
        if (request.getDob() != null) {
            rider.setDob(request.getDob());
        }
        if (request.getGender() != null) {
            rider.setGender(request.getGender());
        }
        if (request.getVehicleNumber() != null) {
            rider.setVehicleNumber(request.getVehicleNumber());
        }
        if (request.getDlNumber() != null) {
            rider.setDlNumber(request.getDlNumber());
        }
        boolean coordinatesChanged = false;
        if (request.getLat() != null) {
            rider.setLat(request.getLat());
            coordinatesChanged = true;
        }
        if (request.getLng() != null) {
            rider.setLng(request.getLng());
            coordinatesChanged = true;
        }

        Rider updatedRider = riderRepository.save(rider);
        evictRiderCache(updatedRider.getRiderId());
        if (coordinatesChanged) {
            asyncGeocodingService.scheduleAddressUpdate(rider.getLat(), rider.getLng(), riderId, "RIDER");
        }
        logger.info("Rider updated successfully for riderId: {}", riderId);
        return updatedRider;
    }

    @Cacheable(value = "riders", key = "'all'")
    public List<Rider> listAllRiders() {
        logger.info("Fetching all active riders");
        List<Rider> riders = riderRepository.findAll();
        logger.info("Found {} riders", riders.size());
        return riders;
    }

    @Cacheable(value = "rider", key = "#riderId")
    public Rider getRiderById(UUID riderId) {
        logger.info("Fetching rider for riderId: {}", riderId);
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found for riderId: {}", riderId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Rider is deactivated for riderId: {}", riderId);
            throw new IllegalArgumentException("Rider is not available");
        }

        logger.info("Successfully fetched rider for riderId: {}", riderId);
        return rider;
    }

    private void evictRiderCache(UUID riderId) {
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache listCache = cacheManager.getCache("riders");
                if (listCache != null) {
                    listCache.clear();
                }
                org.springframework.cache.Cache detailCache = cacheManager.getCache("rider");
                if (detailCache != null && riderId != null) {
                    detailCache.evict(riderId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to evict rider cache for ID: {}", riderId, e);
        }
    }
}
