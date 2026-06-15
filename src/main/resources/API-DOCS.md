# Swigg API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints require JWT Bearer token in the Authorization header (except signup/login endpoints):
```
Authorization: Bearer <jwt_token>
```

---

## Food Management Endpoints

### 1. Create Food
**Endpoint:** `POST /foods/create`

**Authorization:** `RESTAURANT` role required

**Purpose:** Create a new food item for the restaurant

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Biryani",
  "description": "Aromatic rice dish with meat",
  "price": 250.00,
  "category": "MAIN_COURSE",
  "isAvailable": true
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Food created successfully",
  "data": {
    "foodId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Biryani",
    "description": "Aromatic rice dish with meat",
    "price": 250.00,
    "rating": 0.00,
    "reviewCount": 0,
    "category": "MAIN_COURSE",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "isAvailable": true,
    "isActive": true,
    "createdAt": "2026-06-15T10:30:00.000Z",
    "updatedAt": "2026-06-15T10:30:00.000Z"
  }
}
```

**Status Codes:**
- `201 Created` - Food created successfully
- `400 Bad Request` - Invalid input or missing required fields
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have RESTAURANT role
- `500 Internal Server Error` - Server error

**Validation Rules:**
- `name` - Required, non-empty string (1-100 characters)
- `description` - Required, non-empty string (1-500 characters)
- `price` - Required, decimal minimum 0.00
- `category` - Required, valid FoodCategory enum
- `isAvailable` - Optional, defaults to true

**Example cURL:**
```bash
curl -X POST http://localhost:8080/api/foods/create \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Biryani",
    "description": "Aromatic rice dish with meat",
    "price": 250.00,
    "category": "MAIN_COURSE",
    "isAvailable": true
  }'
```

---

### 2. Update Food
**Endpoint:** `PUT /foods/{foodId}/update`

**Authorization:** `RESTAURANT` role required (only owner can update)

**Purpose:** Update an existing food item

**Path Parameters:**
- `foodId` (UUID) - The food ID to update

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Special Biryani",
  "description": "Premium aromatic rice dish",
  "price": 300.00,
  "category": "MAIN_COURSE",
  "isAvailable": true
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Food updated successfully",
  "data": {
    "foodId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Special Biryani",
    "description": "Premium aromatic rice dish",
    "price": 300.00,
    "rating": 0.00,
    "reviewCount": 0,
    "category": "MAIN_COURSE",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "isAvailable": true,
    "isActive": true,
    "createdAt": "2026-06-15T10:30:00.000Z",
    "updatedAt": "2026-06-15T10:35:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Food updated successfully
- `400 Bad Request` - Invalid input or food not found
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have RESTAURANT role or is not the owner
- `500 Internal Server Error` - Server error

**Example cURL:**
```bash
curl -X PUT http://localhost:8080/api/foods/550e8400-e29b-41d4-a716-446655440000/update \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Special Biryani",
    "description": "Premium aromatic rice dish",
    "price": 300.00,
    "category": "MAIN_COURSE",
    "isAvailable": true
  }'
```

---

### 3. Delete Food
**Endpoint:** `DELETE /foods/{foodId}`

**Authorization:** `RESTAURANT` role required (only owner can delete)

**Purpose:** Soft delete a food item (marks as inactive)

**Path Parameters:**
- `foodId` (UUID) - The food ID to delete

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Food deleted successfully"
}
```

**Status Codes:**
- `200 OK` - Food deleted successfully
- `400 Bad Request` - Food not found
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have RESTAURANT role or is not the owner
- `500 Internal Server Error` - Server error

**Example cURL:**
```bash
curl -X DELETE http://localhost:8080/api/foods/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 4. Toggle Food Availability
**Endpoint:** `PATCH /foods/{foodId}/availability`

**Authorization:** `RESTAURANT` role required (only owner can toggle)

**Purpose:** Toggle the availability status of a food item (available/unavailable)

**Path Parameters:**
- `foodId` (UUID) - The food ID to toggle availability

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Food availability toggled successfully",
  "data": {
    "foodId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Biryani",
    "description": "Aromatic rice dish with meat",
    "price": 250.00,
    "rating": 0.00,
    "reviewCount": 0,
    "category": "MAIN_COURSE",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "isAvailable": false,
    "isActive": true,
    "createdAt": "2026-06-15T10:30:00.000Z",
    "updatedAt": "2026-06-15T10:40:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Availability toggled successfully
- `400 Bad Request` - Food not found
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have RESTAURANT role or is not the owner
- `500 Internal Server Error` - Server error

**Example cURL:**
```bash
curl -X PATCH http://localhost:8080/api/foods/550e8400-e29b-41d4-a716-446655440000/availability \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 5. Get Foods by Restaurant
**Endpoint:** `GET /foods/restaurant/{restaurantId}`

**Authorization:** Authenticated user required (any role)

**Purpose:** Retrieve all active foods for a specific restaurant (cached)

**Path Parameters:**
- `restaurantId` (UUID) - The restaurant ID

**Query Parameters:** None

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Foods fetched successfully",
  "count": 5,
  "data": [
    {
      "foodId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Biryani",
      "description": "Aromatic rice dish with meat",
      "price": 250.00,
      "rating": 4.50,
      "reviewCount": 120,
      "category": "MAIN_COURSE",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2026-06-15T10:30:00.000Z",
      "updatedAt": "2026-06-15T10:40:00.000Z"
    },
    {
      "foodId": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Naan",
      "description": "Soft Indian bread",
      "price": 50.00,
      "rating": 4.80,
      "reviewCount": 200,
      "category": "BREAD",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2026-06-15T10:35:00.000Z",
      "updatedAt": "2026-06-15T10:45:00.000Z"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Foods retrieved successfully (cached)
- `401 Unauthorized` - Missing or invalid authentication token
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `restaurant_{restaurantId}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/foods/restaurant/660e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 6. Get Available Foods by Restaurant
**Endpoint:** `GET /foods/restaurant/{restaurantId}/available`

**Authorization:** Authenticated user required (any role)

**Purpose:** Retrieve all active AND available foods for a restaurant (cached)

**Path Parameters:**
- `restaurantId` (UUID) - The restaurant ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Available foods fetched successfully",
  "count": 4,
  "data": [
    {
      "foodId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Biryani",
      "description": "Aromatic rice dish with meat",
      "price": 250.00,
      "rating": 4.50,
      "reviewCount": 120,
      "category": "MAIN_COURSE",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2026-06-15T10:30:00.000Z",
      "updatedAt": "2026-06-15T10:40:00.000Z"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Available foods retrieved successfully (cached)
- `401 Unauthorized` - Missing or invalid authentication token
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `restaurant_available_{restaurantId}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/foods/restaurant/660e8400-e29b-41d4-a716-446655440000/available \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 7. Get Foods by Category
**Endpoint:** `GET /foods/restaurant/{restaurantId}/category/{category}`

**Authorization:** Authenticated user required (any role)

**Purpose:** Retrieve all active foods of a specific category for a restaurant (cached)

**Path Parameters:**
- `restaurantId` (UUID) - The restaurant ID
- `category` (String) - Food category (case-insensitive)

**Valid Categories:**
- `VEG` - Vegetarian
- `NON_VEG` - Non-vegetarian
- `STARTER` - Appetizer/Starter
- `BEVERAGE` - Drink
- `DESSERT` - Sweet dish
- `SALAD` - Salad
- `SOUP` - Soup
- `MAIN_COURSE` - Main dish
- `SIDE_DISH` - Side dish
- `APPETIZER` - Appetizer

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Foods fetched successfully",
  "count": 2,
  "data": [
    {
      "foodId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Biryani",
      "description": "Aromatic rice dish with meat",
      "price": 250.00,
      "rating": 4.50,
      "reviewCount": 120,
      "category": "MAIN_COURSE",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2026-06-15T10:30:00.000Z",
      "updatedAt": "2026-06-15T10:40:00.000Z"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Foods retrieved successfully (cached)
- `400 Bad Request` - Invalid category
- `401 Unauthorized` - Missing or invalid authentication token
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `category_{restaurantId}_{category}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/foods/restaurant/660e8400-e29b-41d4-a716-446655440000/category/MAIN_COURSE \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 8. Get Food by ID
**Endpoint:** `GET /foods/{foodId}`

**Authorization:** Authenticated user required (any role)

**Purpose:** Retrieve a specific food item by ID (cached)

**Path Parameters:**
- `foodId` (UUID) - The food ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Food fetched successfully",
  "data": {
    "foodId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Biryani",
    "description": "Aromatic rice dish with meat",
    "price": 250.00,
    "rating": 4.50,
    "reviewCount": 120,
    "category": "MAIN_COURSE",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "isAvailable": true,
    "isActive": true,
    "createdAt": "2026-06-15T10:30:00.000Z",
    "updatedAt": "2026-06-15T10:40:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Food retrieved successfully (cached)
- `401 Unauthorized` - Missing or invalid authentication token
- `404 Not Found` - Food not found or inactive
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `food_{foodId}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/foods/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 9. Get Top Rated Foods by Category
**Endpoint:** `GET /foods/restaurant/{restaurantId}/category/{category}/rated`

**Authorization:** Authenticated user required (any role)

**Purpose:** Retrieve all active foods of a category sorted by rating (highest first) (cached)

**Path Parameters:**
- `restaurantId` (UUID) - The restaurant ID
- `category` (String) - Food category (case-insensitive)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Top-rated foods fetched successfully",
  "count": 2,
  "data": [
    {
      "foodId": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Naan",
      "description": "Soft Indian bread",
      "price": 50.00,
      "rating": 4.90,
      "reviewCount": 250,
      "category": "BREAD",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2026-06-15T10:35:00.000Z",
      "updatedAt": "2026-06-15T10:45:00.000Z"
    },
    {
      "foodId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Biryani",
      "description": "Aromatic rice dish with meat",
      "price": 250.00,
      "rating": 4.50,
      "reviewCount": 120,
      "category": "MAIN_COURSE",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
      "isAvailable": true,
      "isActive": true,
      "createdAt": "2026-06-15T10:30:00.000Z",
      "updatedAt": "2026-06-15T10:40:00.000Z"
    }
  ]
}
```

**Status Codes:**
- `200 OK` - Top-rated foods retrieved successfully (cached)
- `400 Bad Request` - Invalid category
- `401 Unauthorized` - Missing or invalid authentication token
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `category_rated_{restaurantId}_{category}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/foods/restaurant/660e8400-e29b-41d4-a716-446655440000/category/MAIN_COURSE/rated \
  -H "Authorization: Bearer <jwt_token>"
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "error": "Error message describing what went wrong"
}
```

**Common Error Messages:**
- `"Invalid username or password"` - Authentication failed
- `"Food not found"` - Food ID doesn't exist or is inactive
- `"Food not found or does not belong to this restaurant"` - Authorization failed
- `"Invalid category: INVALID_CATEGORY"` - Invalid food category provided
- `"Invalid or expired verification code"` - TOTP verification failed
- `"Account is deactivated"` - User account is not active
- `"Internal server error"` - Server-side error

---

## Response Codes Reference

| Code | Meaning |
|------|---------|
| 200 | OK - Request succeeded |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Missing or invalid authentication |
| 403 | Forbidden - User lacks required permissions |
| 404 | Not Found - Resource does not exist |
| 500 | Internal Server Error - Server-side error |

---

## Role-Based Access Control

### Food Management (Create, Update, Delete, Toggle Availability)
- **Required Role:** `RESTAURANT`
- **Ownership:** User must own the restaurant that created the food

### Food Retrieval (Get, List, Filter)
- **Required Role:** Any authenticated user (`CUSTOMER`, `RESTAURANT`, `RIDER`, `ADMIN`)
- **Restrictions:** None (all users can view available foods)

---

## Caching Strategy

All GET endpoints implement Redis caching with 1-hour TTL:

| Endpoint | Cache Key | TTL |
|----------|-----------|-----|
| Get Foods by Restaurant | `restaurant_{restaurantId}` | 1 hour |
| Get Available Foods | `restaurant_available_{restaurantId}` | 1 hour |
| Get Foods by Category | `category_{restaurantId}_{category}` | 1 hour |
| Get Food by ID | `food_{foodId}` | 1 hour |
| Get Top Rated by Category | `category_rated_{restaurantId}_{category}` | 1 hour |

Cache is automatically invalidated on:
- Food creation
- Food update
- Food deletion (soft delete)
- Availability toggle

---

## Database Indexes

The Food table includes the following indexes for optimal query performance:

| Index Name | Columns | Type |
|------------|---------|------|
| idx_foods_restaurantid | restaurantid | Single |
| idx_foods_category | category | Single |
| idx_foods_isactive | isactive | Single |
| idx_foods_isavailable | isavailable | Single |
| idx_foods_restaurantid_isactive | restaurantid, isactive | Composite |
| idx_foods_restaurantid_isavailable | restaurantid, isavailable | Composite |

---

## Implementation Notes

1. **Authentication:** JWT tokens must be passed in the `Authorization` header with format `Bearer <token>`
2. **Timestamps:** All timestamps are in UTC (ISO 8601 format)
3. **Rating & Reviews:** Initially set to 0 (can be updated by review service)
4. **Soft Deletion:** Foods are soft-deleted by setting `isActive=false`
5. **Cache Invalidation:** Manual cache clearing on write operations ensures data consistency
6. **Pagination:** Not yet implemented; returns all matching records
7. **Sorting:** Top-rated endpoint sorts by rating in descending order
