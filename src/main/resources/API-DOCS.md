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

## Response DTO Structure

### ApiResponse<T>
All successful endpoints return responses wrapped in the `ApiResponse<T>` DTO:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "count": null
}
```

**Fields:**
- `success` (boolean) - Indicates if operation was successful
- `message` (string) - Human-readable message
- `data` (T) - Generic data payload (type varies by endpoint)
- `count` (integer, optional) - Used only in list endpoints to indicate item count

### Error Response
Error responses include success=false and an error message:

```json
{
  "success": false,
  "message": "Error description"
}
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
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:30:00.000Z"
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
- `name` - Required, non-empty string (3-100 characters)
- `description` - Optional, string (0-500 characters)
- `price` - Required, decimal > 0
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
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:35:00.000Z"
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
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:40:00.000Z"
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

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Foods fetched successfully",
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
      "createdAt": "2026-06-16T10:30:00.000Z",
      "updatedAt": "2026-06-16T10:40:00.000Z"
    }
  ],
  "count": 5
}
```

**Status Codes:**
- `200 OK` - Foods retrieved successfully (cached for 1 hour)
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
      "createdAt": "2026-06-16T10:30:00.000Z",
      "updatedAt": "2026-06-16T10:40:00.000Z"
    }
  ],
  "count": 4
}
```

**Status Codes:**
- `200 OK` - Available foods retrieved successfully (cached for 1 hour)
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
      "createdAt": "2026-06-16T10:30:00.000Z",
      "updatedAt": "2026-06-16T10:40:00.000Z"
    }
  ],
  "count": 2
}
```

**Status Codes:**
- `200 OK` - Foods retrieved successfully (cached for 1 hour)
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
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:40:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Food retrieved successfully (cached for 1 hour)
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
      "createdAt": "2026-06-16T10:35:00.000Z",
      "updatedAt": "2026-06-16T10:45:00.000Z"
    }
  ],
  "count": 2
}
```

**Status Codes:**
- `200 OK` - Top-rated foods retrieved successfully (cached for 1 hour)
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

## Error Handling

### Error Response Format
All error responses follow this format:

```json
{
  "success": false,
  "message": "Error message describing what went wrong"
}
```

### Common Error Messages
- `"Invalid username or password"` - Authentication failed
- `"Food not found"` - Food ID doesn't exist or is inactive
- `"Food not found or does not belong to this restaurant"` - Authorization failed
- `"Invalid category: INVALID_CATEGORY"` - Invalid food category provided
- `"Invalid or expired verification code"` - TOTP verification failed
- `"Account is deactivated"` - User account is not active
- `"Internal server error"` - Server-side error

---

## HTTP Status Codes Reference

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

### Food Write Operations (Create, Update, Delete, Toggle Availability)
- **Required Role:** `RESTAURANT`
- **Ownership:** User must own the restaurant that created the food

### Food Read Operations (Get, List, Filter)
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
- Food creation (all restaurant-related caches)
- Food update (all restaurant and specific food caches)
- Food deletion (all restaurant and specific food caches)
- Availability toggle (all restaurant and specific food caches)

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

## Implementation Details

### Request/Response DTOs
- **FoodRequestDTO**: Contains name, description, price, category, isAvailable fields for create/update operations
- **FoodResponseDTO**: Contains all 11 fields from Food entity including timestamps
- **ApiResponse<T>**: Generic wrapper for successful responses with success flag, message, data, and optional count

### Security
- All write operations require `RESTAURANT` role
- Restaurant ownership is verified for write operations (only owner can modify their foods)
- All read operations require authentication but allow any role

### Timestamps
- All timestamps are in UTC (ISO 8601 format)
- `createdAt` - Set at entity creation, immutable
- `updatedAt` - Set at entity creation, updated on each modification

### Data Initialization
- `rating` - Initially set to 0.00 (can be updated by review service)
- `reviewCount` - Initially set to 0 (can be updated by review service)
- `isActive` - Initially set to true (soft delete sets to false)

### Soft Deletion
- Foods are soft-deleted by setting `isActive=false`
- Deleted foods are excluded from all GET queries
- Deleted foods cannot be updated or toggled

### Pagination
- Not yet implemented; returns all matching records
- Use limit/offset parameters in future versions

### Sorting
- Top-rated endpoint sorts by rating in descending order
- Other endpoints return in creation order

---

## Cart Management Endpoints

### 1. Create Cart
**Endpoint:** `POST /carts/create`

**Authorization:** `CUSTOMER` role required

**Purpose:** Create a new shopping cart for a customer at a specific restaurant

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "restaurantId": "660e8400-e29b-41d4-a716-446655440000"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Cart created successfully",
  "data": {
    "cartId": "770e8400-e29b-41d4-a716-446655440000",
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "foodIds": [],
    "totalPrice": 0.00,
    "status": "ACTIVE",
    "isActive": true,
    "itemCount": 0,
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:30:00.000Z"
  }
}
```

**Status Codes:**
- `201 Created` - Cart created successfully
- `400 Bad Request` - Invalid restaurant or customer not found
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have CUSTOMER role
- `500 Internal Server Error` - Server error

**Validation Rules:**
- `restaurantId` - Required, must be a valid UUID of an active restaurant
- Only one active cart allowed per customer per restaurant

**Example cURL:**
```bash
curl -X POST http://localhost:8080/api/carts/create \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### 2. Get Cart by ID
**Endpoint:** `GET /carts/{cartId}`

**Authorization:** `CUSTOMER` role required (must own cart)

**Purpose:** Retrieve a specific cart by ID (cached)

**Path Parameters:**
- `cartId` (UUID) - The cart ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cart fetched successfully",
  "data": {
    "cartId": "770e8400-e29b-41d4-a716-446655440000",
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "foodIds": ["550e8400-e29b-41d4-a716-446655440000", "550e8400-e29b-41d4-a716-446655440000"],
    "totalPrice": 500.00,
    "status": "ACTIVE",
    "isActive": true,
    "itemCount": 2,
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:35:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Cart retrieved successfully (cached for 1 hour)
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not own this cart
- `404 Not Found` - Cart not found or inactive
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `cart_{cartId}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/carts/770e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 3. Get Active Cart
**Endpoint:** `GET /carts/active`

**Authorization:** `CUSTOMER` role required

**Purpose:** Retrieve the current active cart for the authenticated customer (cached)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Active cart fetched successfully",
  "data": {
    "cartId": "770e8400-e29b-41d4-a716-446655440000",
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "foodIds": ["550e8400-e29b-41d4-a716-446655440000"],
    "totalPrice": 250.00,
    "status": "ACTIVE",
    "isActive": true,
    "itemCount": 1,
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:35:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Active cart retrieved successfully (cached for 1 hour)
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not have CUSTOMER role
- `404 Not Found` - No active cart found
- `500 Internal Server Error` - Server error

**Cache:** Cached with key `customer_active_{customerId}` for 1 hour

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/carts/active \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 4. Add Item to Cart
**Endpoint:** `POST /carts/{cartId}/items/add`

**Authorization:** `CUSTOMER` role required (must own cart)

**Purpose:** Add food items to cart with specified quantity

**Path Parameters:**
- `cartId` (UUID) - The cart ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "foodId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 2
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Item added to cart successfully",
  "data": {
    "cartId": "770e8400-e29b-41d4-a716-446655440000",
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "foodIds": ["550e8400-e29b-41d4-a716-446655440000", "550e8400-e29b-41d4-a716-446655440000"],
    "totalPrice": 500.00,
    "status": "ACTIVE",
    "isActive": true,
    "itemCount": 2,
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:40:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Item added successfully
- `400 Bad Request` - Invalid food, quantity, or cart validation failed
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not own this cart
- `404 Not Found` - Cart or food not found
- `500 Internal Server Error` - Server error

**Validation Rules:**
- `foodId` - Required, must be a valid UUID of an active food
- `quantity` - Required, minimum 1
- Food must belong to the same restaurant as the cart
- Food must be available (isAvailable = true)
- Cart must be in ACTIVE status

**Note on Quantities:** When adding items, duplicates are added to the list (e.g., adding 2 units of food adds the foodId twice)

**Example cURL:**
```bash
curl -X POST http://localhost:8080/api/carts/770e8400-e29b-41d4-a716-446655440000/items/add \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "foodId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2
  }'
```

---

### 5. Remove Item from Cart
**Endpoint:** `DELETE /carts/{cartId}/items/{foodId}`

**Authorization:** `CUSTOMER` role required (must own cart)

**Purpose:** Remove food items from cart by specified quantity

**Path Parameters:**
- `cartId` (UUID) - The cart ID
- `foodId` (UUID) - The food ID to remove

**Query Parameters:**
- `quantity` (integer, optional) - Number of items to remove (default: 1)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Item removed from cart successfully",
  "data": {
    "cartId": "770e8400-e29b-41d4-a716-446655440000",
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "foodIds": [],
    "totalPrice": 0.00,
    "status": "ACTIVE",
    "isActive": true,
    "itemCount": 0,
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:45:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Item removed successfully
- `400 Bad Request` - Food not in cart or invalid quantity
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not own this cart
- `404 Not Found` - Cart not found
- `500 Internal Server Error` - Server error

**Example cURL:**
```bash
curl -X DELETE http://localhost:8080/api/carts/770e8400-e29b-41d4-a716-446655440000/items/550e8400-e29b-41d4-a716-446655440000?quantity=1 \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 6. Checkout (Place Order)
**Endpoint:** `POST /carts/{cartId}/checkout`

**Authorization:** `CUSTOMER` role required (must own cart)

**Purpose:** Convert cart to order and change status to ORDERED

**Path Parameters:**
- `cartId` (UUID) - The cart ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "cartId": "770e8400-e29b-41d4-a716-446655440000",
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
    "foodIds": ["550e8400-e29b-41d4-a716-446655440000"],
    "totalPrice": 250.00,
    "status": "ORDERED",
    "isActive": true,
    "itemCount": 1,
    "createdAt": "2026-06-16T10:30:00.000Z",
    "updatedAt": "2026-06-16T10:50:00.000Z"
  }
}
```

**Status Codes:**
- `200 OK` - Order placed successfully
- `400 Bad Request` - Cart is empty or not in ACTIVE status
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not own this cart
- `404 Not Found` - Cart not found
- `500 Internal Server Error` - Server error

**Validation Rules:**
- Cart must not be empty
- Cart must be in ACTIVE status
- Cart must be active (isActive = true)

**Example cURL:**
```bash
curl -X POST http://localhost:8080/api/carts/770e8400-e29b-41d4-a716-446655440000/checkout \
  -H "Authorization: Bearer <jwt_token>"
```

---

### 7. Delete Cart
**Endpoint:** `DELETE /carts/{cartId}`

**Authorization:** `CUSTOMER` role required (must own cart)

**Purpose:** Soft delete a cart (marks as inactive)

**Path Parameters:**
- `cartId` (UUID) - The cart ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cart deleted successfully"
}
```

**Status Codes:**
- `200 OK` - Cart deleted successfully
- `400 Bad Request` - Cart not found
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - User does not own this cart
- `404 Not Found` - Cart not found
- `500 Internal Server Error` - Server error

**Example cURL:**
```bash
curl -X DELETE http://localhost:8080/api/carts/770e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <jwt_token>"
```

---

## Cart Module Implementation Details

### Request/Response DTOs
- **CartRequestDTO**: Contains restaurantId for cart creation
- **AddToCartRequestDTO**: Contains foodId and quantity for adding items
- **CartResponseDTO**: Contains all cart information including foodIds list, totalPrice, status, and itemCount

### CartStatus Enum
- `ACTIVE` - Cart has items and is being used
- `ABANDONED` - Cart was not updated for a period
- `ORDERED` - Cart has been converted to an order

### Cart Item Storage
- `foodIds` - Stored as ElementCollection with duplicates for each quantity
- Example: Adding 2 units of food1 stores [food1, food1] in the list

### Price Calculation
- Total price is calculated by summing prices of all food items in the cart
- Calculation happens automatically when items are added/removed
- Uses actual food prices from the Food entity

### Security
- All operations require `CUSTOMER` role
- Customers can only access/modify their own carts
- Food items must belong to the same restaurant as the cart
- Food must be active and available to be added

### Caching Strategy
- Individual cart caching: key `cart_{cartId}` for 1 hour
- Active cart caching: key `customer_active_{customerId}` for 1 hour
- Cache is invalidated on create, add item, remove item, checkout, delete

### Database Indexes
The Cart table includes the following indexes for optimal query performance:

| Index Name | Columns | Type |
|------------|---------|------|
| idx_carts_customerid | customerid | Single |
| idx_carts_restaurantid | restaurantid | Single |
| idx_carts_isactive | isactive | Single |
| idx_carts_status | status | Single |
| idx_carts_customerid_isactive | customerid, isactive | Composite |

### Validation Logic
- **Restaurant Validation**: Restaurant must exist and be active
- **Customer Validation**: Customer must exist and be active
- **Food Validation**: Food must exist, be active, be available, and belong to same restaurant
- **Quantity Validation**: Must be positive integer
- **Empty Cart Check**: Cannot checkout with empty cart
- **Status Check**: Can only add/remove items from ACTIVE status carts

### Soft Deletion
- Carts are soft-deleted by setting `isActive=false`
- Deleted carts are excluded from all queries
- Original cart data is preserved for historical purposes
