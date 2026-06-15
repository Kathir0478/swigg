package com.swigg.customer;

import com.swigg.auth.TokenResponseDTO;
import com.swigg.common.ApiResponse;
import com.swigg.common.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/register/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CustomerInitResponseDTO>> registerRequest(
            Authentication authentication,
            @RequestBody CustomerRegisterRequestDTO request) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Customer registration request received for userId: {}", userId);
            CustomerInitResponseDTO response = customerService.initiateRegister(userId, request);
            logger.info("Customer registration TOTP code generated for userId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer registration request failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer registration request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/register/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> registerVerify(
            Authentication authentication,
            @RequestBody CustomerRegisterVerifyRequestDTO request) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Customer registration verification request received for userId: {}", userId);
            Customer customer = customerService.completeRegister(userId, request.getOtpCode());
            logger.info("Customer registration verified for userId: {}", userId);
            return ApiResponses.created("Customer registered and verified successfully", mapToResponseDTO(customer));
        } catch (IllegalArgumentException e) {
            logger.warn("Customer registration verification failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer registration verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<ApiResponse<CustomerInitResponseDTO>> loginRequest(
            @RequestBody CustomerLoginRequestDTO request) {
        try {
            logger.info("Customer login request received for phone: {}", request.getPhoneNumber());
            CustomerInitResponseDTO response = customerService.initiateLogin(request);
            logger.info("Customer login TOTP code generated for phone: {}", request.getPhoneNumber());
            return ApiResponses.ok("Verification code sent to your mobile number", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer login request failed: {}", e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer login request error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> loginVerify(
            @RequestBody CustomerLoginVerifyRequestDTO request) {
        try {
            logger.info("Customer login verification request received for phone: {}", request.getPhoneNumber());
            Customer customer = customerRepository.findByUser_PhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            TokenResponseDTO response = customerService.completeLogin(customer.getCustomerId(), request.getOtpCode());
            logger.info("Customer login successful for phone: {}", request.getPhoneNumber());
            return ApiResponses.ok("Login successful", response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer login verification failed: {}", e.getMessage());
            return ApiResponses.unauthorized(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer login verification error", e);
            return ApiResponses.internalError();
        }
    }

    @PostMapping("/delete/request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> requestDeletion(Authentication authentication) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Customer deletion OTP request received for userId: {}", userId);
            customerService.requestDeletion(userId);
            logger.info("Customer deletion OTP sent for userId: {}", userId);
            return ApiResponses.ok("Verification code sent to your mobile number");
        } catch (IllegalArgumentException e) {
            logger.warn("Customer deletion OTP request failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer deletion OTP request error", e);
            return ApiResponses.internalError();
        }
    }

    @DeleteMapping("/delete/complete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            Authentication authentication,
            @RequestBody CustomerDeleteVerifyDTO request) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            logger.info("Customer deletion request received for userId: {}", userId);
            customerService.completeDelete(userId, request);
            logger.info("Customer deleted successfully for userId: {}", userId);
            return ApiResponses.ok("Customer account deactivated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Customer deletion failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer deletion error", e);
            return ApiResponses.internalError();
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            Authentication authentication,
            @RequestBody CustomerUpdateRequestDTO request) {
        try {
            UUID customerId = UUID.fromString(authentication.getName());
            logger.info("Customer update request received for customerId: {}", customerId);
            Customer customer = customerService.updateCustomer(customerId, request);
            logger.info("Customer updated successfully for customerId: {}", customerId);
            return ApiResponses.ok("Customer updated successfully", mapToResponseDTO(customer));
        } catch (IllegalArgumentException e) {
            logger.warn("Customer update failed: {}", e.getMessage());
            return ApiResponses.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer update error", e);
            return ApiResponses.internalError();
        }
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getCustomer(@PathVariable UUID customerId) {
        try {
            logger.info("Fetching customer for customerId: {}", customerId);
            Customer customer = customerService.getCustomerById(customerId);
            logger.info("Successfully fetched customer for customerId: {}", customerId);
            return ApiResponses.ok("Customer fetched successfully", mapToResponseDTO(customer));
        } catch (IllegalArgumentException e) {
            logger.warn("Customer fetch failed: {}", e.getMessage());
            return ApiResponses.notFound(e.getMessage());
        } catch (Exception e) {
            logger.error("Customer fetch error", e);
            return ApiResponses.internalError();
        }
    }

    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .customerId(customer.getCustomerId())
                .userId(customer.getUser().getUserId())
                .name(customer.getName())
                .address(customer.getAddress())
                .dob(customer.getDob())
                .gender(customer.getGender())
                .lat(customer.getLat())
                .lng(customer.getLng())
                .isActive(customer.getIsActive())
                .isVerified(customer.getIsVerified())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
