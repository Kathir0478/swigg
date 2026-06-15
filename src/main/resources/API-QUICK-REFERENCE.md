# SWIGG API - Quick Reference Guide

## Endpoint Summary by Category

### Total Endpoints: 35
- Auth: 2 endpoints
- Restaurants: 9 endpoints
- Customers: 9 endpoints
- Riders: 9 endpoints

---

## Auth Endpoints (Unprotected)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/register` | User Registration |
| POST | `/api/auth/login` | User Login |

---

## Restaurant Endpoints

### Public Endpoints (No Auth Required)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/restaurants/list` | List all restaurants |
| GET | `/api/restaurants/{restaurantId}` | Get restaurant details |
| POST | `/api/restaurants/login/request` | Restaurant login - Request OTP |
| POST | `/api/restaurants/login/verify` | Restaurant login - Verify OTP |

### Protected Endpoints (Role: USER)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/restaurants/register/request` | Register - Request OTP |
| POST | `/api/restaurants/register/verify` | Register - Verify OTP |

### Protected Endpoints (Role: RESTAURANT)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| PUT | `/api/restaurants/update` | Update restaurant profile |
| POST | `/api/restaurants/delete/request` | Delete - Request OTP |
| DELETE | `/api/restaurants/delete/complete` | Delete - Verify OTP |

---

## Customer Endpoints

### Public Endpoints (No Auth Required)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/customers/list` | List all customers |
| GET | `/api/customers/{customerId}` | Get customer details |
| POST | `/api/customers/login/request` | Customer login - Request OTP |
| POST | `/api/customers/login/verify` | Customer login - Verify OTP |

### Protected Endpoints (Role: USER)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/customers/register/request` | Register - Request OTP |
| POST | `/api/customers/register/verify` | Register - Verify OTP |

### Protected Endpoints (Role: CUSTOMER)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| PUT | `/api/customers/update` | Update customer profile |
| POST | `/api/customers/delete/request` | Delete - Request OTP |
| DELETE | `/api/customers/delete/complete` | Delete - Verify OTP |

---

## Rider Endpoints

### Public Endpoints (No Auth Required)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/riders/list` | List all riders |
| GET | `/api/riders/{riderId}` | Get rider details |
| POST | `/api/riders/login/request` | Rider login - Request OTP |
| POST | `/api/riders/login/verify` | Rider login - Verify OTP |

### Protected Endpoints (Role: USER)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/riders/register/request` | Register - Request OTP |
| POST | `/api/riders/register/verify` | Register - Verify OTP |

### Protected Endpoints (Role: RIDER)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| PUT | `/api/riders/update` | Update rider profile |
| POST | `/api/riders/delete/request` | Delete - Request OTP |
| DELETE | `/api/riders/delete/complete` | Delete - Verify OTP |

---

## Workflow Comparison

### Registration Workflow (All Entities)
```
1. POST /register/request          → Receive masked phone + message
2. POST /register/verify           → Verify OTP, get registered entity ID
```

### Login Workflow (All Entities)
```
1. POST /login/request             → Receive masked phone + message
2. POST /login/verify              → Verify OTP, receive JWT tokens
```

### Delete Workflow (All Entities)
```
1. POST /delete/request            → Receive masked phone + message
2. DELETE /delete/complete         → Verify OTP, deactivate account
```

### Update Workflow (All Entities)
```
1. PUT /update                     → Update profile information
```

---

## Role-Based Access Control

| Role | Protected Endpoints | Operations |
|------|-------------------|-----------|
| **ADMIN** | All | All operations |
| **USER** | Register endpoints | Can register as restaurant/customer/rider |
| **RESTAURANT** | Restaurant endpoints | Manage restaurant profile + delete |
| **CUSTOMER** | Customer endpoints | Manage customer profile + delete |
| **RIDER** | Rider endpoints | Manage rider profile + delete |

---

## HTTP Status Codes

| Code | Meaning | Example Use |
|------|---------|------------|
| 200 | OK | Successful login, update, list operations |
| 201 | Created | Successful registration |
| 400 | Bad Request | Invalid input, invalid OTP |
| 401 | Unauthorized | Invalid credentials, missing token |
| 403 | Forbidden | Insufficient role permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists |
| 500 | Server Error | Internal server error |

---

## Authentication Headers Format

```
Authorization: Bearer <JWT_TOKEN>
```

**Example:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Request/Response Content Type

All requests and responses use:
```
Content-Type: application/json
```

---

## Common Request Body Examples

### Coordinates
```json
{
  "lat": 28.6139,
  "lng": 77.2090
}
```

### OTP Verification
```json
{
  "otpCode": "123456"
}
```

### Gender Values
```
MALE
FEMALE
UNDEFINED
```

---

## Entity Details Required

### Restaurant
- Name (auto from username)
- Description
- Latitude & Longitude
- Opening/Closing time
- Image URL

### Customer
- Name (auto from username)
- Address
- Date of Birth
- Gender
- Latitude & Longitude

### Rider
- Name (auto from username)
- Address
- Date of Birth
- Gender
- Latitude & Longitude
- Vehicle Number
- DL Number

---

## API Integration Checklist

- [ ] Implement user registration
- [ ] Implement user login
- [ ] Handle JWT token generation and refresh
- [ ] Implement restaurant registration/verification
- [ ] Implement customer registration/verification
- [ ] Implement rider registration/verification
- [ ] Implement profile update endpoints
- [ ] Implement account deletion endpoints
- [ ] Implement list/get endpoints
- [ ] Add role-based access control checks
- [ ] Add OTP generation and verification
- [ ] Add rate limiting
- [ ] Add request/response validation
- [ ] Add error handling and logging

---

## Testing Recommendations

### Unit Tests
- OTP generation and validation
- Password encryption/decryption
- Coordinate validation
- Field validation

### Integration Tests
- Full registration workflow
- Full login workflow
- Full delete workflow
- Profile update operations
- Token refresh operations

### API Tests
- All CRUD operations
- Role-based access control
- Error scenarios
- Rate limiting
- Concurrent requests

---

## Environment Configuration

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/swigg
spring.datasource.username=postgres
spring.datasource.password=password

# JWT
jwt.secret=your-secret-key
jwt.expiration=3600

# TOTP (OTP)
totp.window=1

# Geocoding
geocoding.apiKey=your-api-key
```

---

## Rate Limiting

- **Standard Endpoints**: 100 requests/minute per IP
- **Auth Endpoints**: 10 requests/minute per IP
- **Login Endpoints**: 5 requests/minute per IP

---

## Versioning

**Current API Version**: 1.0
**Last Updated**: 2024-01-15

---

## Support & Documentation

For detailed endpoint documentation, see:
- `API-DOCS.md` - Comprehensive markdown documentation
- `API-DOCS.json` - Machine-readable JSON format

For database schema, see:
- `create_db.sql` - Complete database schema
