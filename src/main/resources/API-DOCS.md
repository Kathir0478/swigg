# SWIGG API Documentation

## Overview
This document contains detailed information about all API endpoints in the SWIGG application including authentication, restaurants, customers, and riders management.

---

## Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [Restaurant Endpoints](#restaurant-endpoints)
3. [Customer Endpoints](#customer-endpoints)
4. [Rider Endpoints](#rider-endpoints)

---

## Authentication Endpoints

### 1. User Registration
**Endpoint:** `POST /api/auth/register`
- **Purpose:** Register a new user account
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "username": "string",
    "phoneNumber": "string",
    "password": "string"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "message": "User registered successfully",
    "userId": "UUID"
  }
  ```
- **Error Response (400/409):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** None required

---

### 2. User Login
**Endpoint:** `POST /api/auth/login`
- **Purpose:** Authenticate user and generate JWT tokens
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "accessToken": "JWT_TOKEN",
    "refreshToken": "JWT_TOKEN",
    "expiresIn": 3600
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid credentials"
  }
  ```
- **Authorization:** None required

---

### 3. Refresh Token
**Endpoint:** `POST /api/auth/refresh`
- **Purpose:** Generate new access token using refresh token
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Requires valid refresh token)
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "refreshToken": "JWT_TOKEN"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "accessToken": "JWT_TOKEN",
    "expiresIn": 3600
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid refresh token"
  }
  ```
- **Authorization:** Refresh Token (Bearer)

---

## Restaurant Endpoints

### 1. Register Restaurant - Request
**Endpoint:** `POST /api/restaurants/register/request`
- **Purpose:** Initiate restaurant registration and generate OTP verification code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: USER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "description": "string",
    "lat": 28.6139,
    "lng": 77.2090,
    "openTime": "2024-01-01T09:00:00",
    "closeTime": "2024-01-01T23:00:00",
    "imageUrl": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: USER

---

### 2. Register Restaurant - Verify
**Endpoint:** `POST /api/restaurants/register/verify`
- **Purpose:** Verify restaurant registration with OTP code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: USER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "otpCode": "string"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "message": "Restaurant registered and verified successfully",
    "restaurantId": "UUID"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: USER

---

### 3. Restaurant Login - Request
**Endpoint:** `POST /api/restaurants/login/request`
- **Purpose:** Initiate restaurant login and generate OTP verification code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid username or password"
  }
  ```
- **Authorization:** None required

---

### 4. Restaurant Login - Verify
**Endpoint:** `POST /api/restaurants/login/verify`
- **Purpose:** Verify restaurant login with OTP and issue tokens
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "phoneNumber": "string",
    "otpCode": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "accessToken": "JWT_TOKEN",
    "refreshToken": "JWT_TOKEN",
    "expiresIn": 3600
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** None required

---

### 5. Delete Restaurant - Request
**Endpoint:** `POST /api/restaurants/delete/request`
- **Purpose:** Request restaurant account deletion and generate OTP code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: RESTAURANT)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: RESTAURANT

---

### 6. Delete Restaurant - Complete
**Endpoint:** `DELETE /api/restaurants/delete/complete`
- **Purpose:** Complete restaurant account deletion with OTP verification
- **HTTP Method:** DELETE
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: RESTAURANT)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "otpCode": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Restaurant account deactivated successfully"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: RESTAURANT

---

### 7. Update Restaurant
**Endpoint:** `PUT /api/restaurants/update`
- **Purpose:** Update restaurant information (name, description, location, etc.)
- **HTTP Method:** PUT
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: RESTAURANT)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "name": "string",
    "description": "string",
    "openTime": "HH:mm:ss",
    "closeTime": "HH:mm:ss",
    "imageUrl": "string",
    "lat": 28.6139,
    "lng": 77.2090
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Restaurant updated successfully",
    "restaurantId": "UUID"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: RESTAURANT

---

### 8. List All Restaurants
**Endpoint:** `GET /api/restaurants/list`
- **Purpose:** Retrieve list of all active restaurants
- **HTTP Method:** GET
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Query Parameters:** None
- **Response (200 OK):**
  ```json
  [
    {
      "restaurantId": "UUID",
      "name": "string",
      "description": "string",
      "address": "string",
      "lat": 28.6139,
      "lng": 77.2090,
      "rating": 4.5,
      "isActive": true,
      "isVerified": true
    }
  ]
  ```
- **Error Response (500):**
  ```json
  {
    "error": "Internal server error"
  }
  ```
- **Authorization:** None required

---

### 9. Get Restaurant by ID
**Endpoint:** `GET /api/restaurants/{restaurantId}`
- **Purpose:** Retrieve specific restaurant details
- **HTTP Method:** GET
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Path Parameters:**
  - `restaurantId` (UUID): Restaurant identifier
- **Response (200 OK):**
  ```json
  {
    "restaurantId": "UUID",
    "name": "string",
    "description": "string",
    "address": "string",
    "lat": 28.6139,
    "lng": 77.2090,
    "openTime": "HH:mm:ss",
    "closeTime": "HH:mm:ss",
    "rating": 4.5,
    "reviewCount": 150,
    "isActive": true,
    "isVerified": true,
    "createdAt": "2024-01-01T10:00:00"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Restaurant not found"
  }
  ```
- **Authorization:** None required

---

## Customer Endpoints

### 1. Register Customer - Request
**Endpoint:** `POST /api/customers/register/request`
- **Purpose:** Initiate customer registration and generate OTP verification code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: USER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "address": "string",
    "dob": "2000-01-01T00:00:00",
    "gender": "MALE|FEMALE|UNDEFINED",
    "lat": 28.6139,
    "lng": 77.2090
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: USER

---

### 2. Register Customer - Verify
**Endpoint:** `POST /api/customers/register/verify`
- **Purpose:** Verify customer registration with OTP code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: USER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "otpCode": "string"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "message": "Customer registered and verified successfully",
    "customerId": "UUID"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: USER

---

### 3. Customer Login - Request
**Endpoint:** `POST /api/customers/login/request`
- **Purpose:** Initiate customer login and generate OTP verification code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "username": "string",
    "password": "string",
    "phoneNumber": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid username or password"
  }
  ```
- **Authorization:** None required

---

### 4. Customer Login - Verify
**Endpoint:** `POST /api/customers/login/verify`
- **Purpose:** Verify customer login with OTP and issue tokens
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "phoneNumber": "string",
    "otpCode": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "accessToken": "JWT_TOKEN",
    "refreshToken": "JWT_TOKEN",
    "expiresIn": 3600
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** None required

---

### 5. Delete Customer - Request
**Endpoint:** `POST /api/customers/delete/request`
- **Purpose:** Request customer account deletion and generate OTP code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: CUSTOMER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: CUSTOMER

---

### 6. Delete Customer - Complete
**Endpoint:** `DELETE /api/customers/delete/complete`
- **Purpose:** Complete customer account deletion with OTP verification
- **HTTP Method:** DELETE
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: CUSTOMER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "otpCode": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Customer account deactivated successfully"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: CUSTOMER

---

### 7. Update Customer
**Endpoint:** `PUT /api/customers/update`
- **Purpose:** Update customer profile information
- **HTTP Method:** PUT
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: CUSTOMER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "name": "string",
    "address": "string",
    "dob": "2000-01-01T00:00:00",
    "gender": "MALE|FEMALE|UNDEFINED",
    "lat": 28.6139,
    "lng": 77.2090
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Customer updated successfully",
    "customerId": "UUID"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: CUSTOMER

---

### 8. List All Customers
**Endpoint:** `GET /api/customers/list`
- **Purpose:** Retrieve list of all active customers
- **HTTP Method:** GET
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Response (200 OK):**
  ```json
  [
    {
      "customerId": "UUID",
      "name": "string",
      "address": "string",
      "dob": "2000-01-01T00:00:00",
      "gender": "MALE|FEMALE|UNDEFINED",
      "lat": 28.6139,
      "lng": 77.2090,
      "isActive": true,
      "isVerified": true
    }
  ]
  ```
- **Error Response (500):**
  ```json
  {
    "error": "Internal server error"
  }
  ```
- **Authorization:** None required

---

### 9. Get Customer by ID
**Endpoint:** `GET /api/customers/{customerId}`
- **Purpose:** Retrieve specific customer profile details
- **HTTP Method:** GET
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Path Parameters:**
  - `customerId` (UUID): Customer identifier
- **Response (200 OK):**
  ```json
  {
    "customerId": "UUID",
    "name": "string",
    "address": "string",
    "dob": "2000-01-01T00:00:00",
    "gender": "MALE|FEMALE|UNDEFINED",
    "lat": 28.6139,
    "lng": 77.2090,
    "isActive": true,
    "isVerified": true,
    "createdAt": "2024-01-01T10:00:00"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Customer not found"
  }
  ```
- **Authorization:** None required

---

## Rider Endpoints

### 1. Register Rider - Request
**Endpoint:** `POST /api/riders/register/request`
- **Purpose:** Initiate rider registration and generate OTP verification code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: USER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "address": "string",
    "dob": "2000-01-01T00:00:00",
    "gender": "MALE|FEMALE|UNDEFINED",
    "lat": 28.6139,
    "lng": 77.2090,
    "vehicleNumber": "string",
    "dlNumber": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: USER

---

### 2. Register Rider - Verify
**Endpoint:** `POST /api/riders/register/verify`
- **Purpose:** Verify rider registration with OTP code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: USER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "otpCode": "string"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "message": "Rider registered and verified successfully",
    "riderId": "UUID"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: USER

---

### 3. Rider Login - Request
**Endpoint:** `POST /api/riders/login/request`
- **Purpose:** Initiate rider login and generate OTP verification code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid username or password"
  }
  ```
- **Authorization:** None required

---

### 4. Rider Login - Verify
**Endpoint:** `POST /api/riders/login/verify`
- **Purpose:** Verify rider login with OTP and issue tokens
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Request Body:**
  ```json
  {
    "phoneNumber": "string",
    "otpCode": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "accessToken": "JWT_TOKEN",
    "refreshToken": "JWT_TOKEN",
    "expiresIn": 3600
  }
  ```
- **Error Response (401):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** None required

---

### 5. Delete Rider - Request
**Endpoint:** `POST /api/riders/delete/request`
- **Purpose:** Request rider account deletion and generate OTP code
- **HTTP Method:** POST
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: RIDER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Verification code sent to your mobile number",
    "maskedPhoneNumber": "XXXX1234"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: RIDER

---

### 6. Delete Rider - Complete
**Endpoint:** `DELETE /api/riders/delete/complete`
- **Purpose:** Complete rider account deletion with OTP verification
- **HTTP Method:** DELETE
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: RIDER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "otpCode": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Rider account deactivated successfully"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Invalid or expired verification code"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: RIDER

---

### 7. Update Rider
**Endpoint:** `PUT /api/riders/update`
- **Purpose:** Update rider profile information and vehicle details
- **HTTP Method:** PUT
- **API Type:** REST/HTTP
- **Protected:** Yes (Role: RIDER)
- **Sync/Async:** Synchronous
- **Request Headers:**
  ```
  Authorization: Bearer <ACCESS_TOKEN>
  ```
- **Request Body:**
  ```json
  {
    "name": "string",
    "address": "string",
    "dob": "2000-01-01T00:00:00",
    "gender": "MALE|FEMALE|UNDEFINED",
    "lat": 28.6139,
    "lng": 77.2090,
    "vehicleNumber": "string",
    "dlNumber": "string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "message": "Rider updated successfully",
    "riderId": "UUID"
  }
  ```
- **Error Response (400/401):**
  ```json
  {
    "error": "string"
  }
  ```
- **Authorization:** Bearer Token (ACCESS_TOKEN), Role: RIDER

---

### 8. List All Riders
**Endpoint:** `GET /api/riders/list`
- **Purpose:** Retrieve list of all active riders
- **HTTP Method:** GET
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Response (200 OK):**
  ```json
  [
    {
      "riderId": "UUID",
      "name": "string",
      "address": "string",
      "dob": "2000-01-01T00:00:00",
      "gender": "MALE|FEMALE|UNDEFINED",
      "vehicleNumber": "string",
      "dlNumber": "string",
      "lat": 28.6139,
      "lng": 77.2090,
      "isActive": true,
      "isVerified": true
    }
  ]
  ```
- **Error Response (500):**
  ```json
  {
    "error": "Internal server error"
  }
  ```
- **Authorization:** None required

---

### 9. Get Rider by ID
**Endpoint:** `GET /api/riders/{riderId}`
- **Purpose:** Retrieve specific rider profile and vehicle details
- **HTTP Method:** GET
- **API Type:** REST/HTTP
- **Protected:** No
- **Sync/Async:** Synchronous
- **Path Parameters:**
  - `riderId` (UUID): Rider identifier
- **Response (200 OK):**
  ```json
  {
    "riderId": "UUID",
    "name": "string",
    "address": "string",
    "dob": "2000-01-01T00:00:00",
    "gender": "MALE|FEMALE|UNDEFINED",
    "vehicleNumber": "string",
    "dlNumber": "string",
    "lat": 28.6139,
    "lng": 77.2090,
    "isActive": true,
    "isVerified": true,
    "createdAt": "2024-01-01T10:00:00"
  }
  ```
- **Error Response (400):**
  ```json
  {
    "error": "Rider not found"
  }
  ```
- **Authorization:** None required

---

## Common Error Codes

| Status Code | Description |
|------------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input or parameters |
| 401 | Unauthorized - Invalid or missing authentication |
| 403 | Forbidden - User lacks required permissions/role |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error - Server-side error |

---

## Authentication

All protected endpoints require:
- **Header:** `Authorization: Bearer <ACCESS_TOKEN>`
- **Token Type:** JWT (JSON Web Token)
- **Token Expiry:** 1 hour (configurable)
- **Refresh:** Use refresh token to obtain new access token

---

## Rate Limiting

- Default: 100 requests per minute per IP
- Auth endpoints: 10 requests per minute per IP
- Apply for all endpoints

---

## Base URL

```
http://localhost:8080/api
```

---

## Content Type

All requests and responses use:
```
Content-Type: application/json
```

---

## Version

**API Version:** 1.0  
**Last Updated:** 2024-01-15
