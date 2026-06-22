package com.swigg.customer;

import com.swigg.auth.AuthService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthService authService;

    @PostMapping("/register/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerRequest(Authentication authentication, @RequestBody CustomerRegisterRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Customer registration request received for userId: {}", userId);
        try {
            CustomerInitResponseDTO response = customerService.initiateRegister(userId, request);
            logger.info("Customer registration TOTP code generated for userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer registration request failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerVerify(Authentication authentication, @RequestBody CustomerRegisterVerifyRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Customer registration verification request received for userId: {}", userId);
        try {
            Customer customer = customerService.completeRegister(userId, request.getOtpCode());
            logger.info("Customer registration verified for userId: {}", userId);
            
            // Generate new JWT tokens with CUSTOMER role
            User user = customer.getUser();
            TokenResponseDTO tokens = authService.generateTokensForUser(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Customer registered and verified successfully",
                    "customerId", customer.getCustomerId(),
                    "accessToken", tokens.getAccessToken(),
                    "refreshToken", tokens.getRefreshToken()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Customer registration verification failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login/request")
    public ResponseEntity<?> loginRequest(@RequestBody CustomerLoginRequestDTO request) {
        logger.info("Customer login request received for username: {}", request.getUsername());
        try {
            CustomerInitResponseDTO response = customerService.initiateLogin(request);
            logger.info("Customer login TOTP code generated for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer login request failed for username: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@RequestBody CustomerLoginVerifyRequestDTO request) {
        logger.info("Customer login verification request received for phone: {}", request.getPhoneNumber());
        try {
            Customer tempCustomer = customerRepository.findByUser_PhoneNumberAndUser_IsActive(request.getPhoneNumber(), true)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            TokenResponseDTO response = customerService.completeLogin(tempCustomer.getCustomerId(), request.getOtpCode());
            logger.info("Customer login successful and tokens issued for phone: {}", request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer login verification failed for phone: {}. Reason: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/delete/request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteRequest(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Customer delete OTP request received for customerId: {}", userId);
        try {
            var response = customerService.requestDeletion(userId);
            logger.info("Customer delete OTP sent for customerId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Customer delete OTP request failed for customerId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/delete/complete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteComplete(Authentication authentication, @RequestBody CustomerDeleteVerifyDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Customer delete completion request received for userId: {}", userId);
        try {
            customerService.completeDelete(userId, request);
            logger.info("Customer deleted successfully for userId: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Customer account deactivated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Customer deletion failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateCustomer(Authentication authentication, @RequestBody CustomerUpdateRequestDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Customer update request received for userId: {}", userId);
        try {
            Customer updatedCustomer = customerService.updateCustomer(userId, request);
            logger.info("Customer updated successfully for userId: {}", userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Customer updated successfully",
                    "customerId", updatedCustomer.getCustomerId()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Customer update failed for userId: {}. Reason: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listCustomers() {
        logger.info("List customers request received");
        try {
            List<Customer> customers = customerService.listAllCustomers();
            logger.info("Successfully fetched {} customers", customers.size());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            logger.warn("Failed to list customers. Reason: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCustomer(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        logger.info("Get customer request received for userId: {}", userId);
        try {
            CustomerResponseDTO customer = customerService.getCustomerByUserId(userId);
            logger.info("Successfully fetched customer for userId: {}", userId);
            return ResponseEntity.ok(customer);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to fetch customer for userId: {}. Reason: {}", userId, e.getMessage());
            if (e.getMessage().contains("not available")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
