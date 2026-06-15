# Swigg Application - Restaurant Module

A Spring Boot application with a **Restaurant module** featuring JPA, PostgreSQL database integration, Spring Security with JWT authentication, a global exception handling system, and SLF4J logging.

---

## Technical Stack
- **Language/Platform**: Java 21
- **Framework**: Spring Boot 3.3.0
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens) with HS256 encryption

---

## Getting Started

### Prerequisites
1. **Java Development Kit (JDK)**: Ensure JDK 21 is installed.
2. **Apache Maven**: Ensure Maven is installed and added to your system path.
3. **PostgreSQL**: Ensure PostgreSQL database server is running locally on port `5432`.

### Database Setup
1. Log in to your PostgreSQL instance (e.g., using `psql` or `pgAdmin`).
2. Create a database named `swigg_db`:
   ```sql
   CREATE DATABASE swigg_db;
   ```
3. By default, the application is configured to connect to PostgreSQL with the following credentials (defined in `src/main/resources/application.properties`):
   - **URL**: `jdbc:postgresql://localhost:5432/swigg_db`
   - **Username**: `postgres`
   - **Password**: `postgres`
   
   *Note: If your local PostgreSQL credentials differ, update the properties in [application.properties](file:///c:/Users/kathi/IdeaProjects/swigg/src/main/resources/application.properties) accordingly.*

### Build and Compilation
Build the application and download dependencies:
```bash
mvn clean compile
```

### Running the Application
Run the Spring Boot application using Maven:
```bash
mvn spring-boot:run
```
The application will start on port `8080` (base URL: `http://localhost:8080`).

---

## Security & Authentication

All API endpoints under `/api/restaurants/**` are secured using Spring Security and JWT. The application enforces Role-Based Access Control (RBAC) with the following roles:
- **`CUSTOMER`**: Can view/search restaurants.
- **`RIDER`**: Can view/search restaurants.
- **`RESTAURANT`**: Full permissions (can view, create, update, and soft-delete restaurants).

### Generating a Demo Token
To facilitate testing different roles, the open demo token generator endpoint accepts a JSON request body:
- **URL**: `http://localhost:8080/api/auth/token`
- **Method**: `POST`
- **Headers**: `Content-Type: application/json`

**Sample Request (Generating a RIDER role token)**:
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "kathir",
    "role": "RIDER"
  }'
```

**Sample Output**:
```json
{
  "tokenType": "Bearer",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYXRoaXIiLCJyb2xlIjoiUklERVIiLCJpYXQiOjE3ODE5ODU3ODksImV4cCI6MTc4MjA3MjE4OX0..."
}
```

### Using the Token
For all secured API requests, include the token in the `Authorization` header:
```http
Authorization: Bearer <YOUR_GENERATED_TOKEN>
```

---

## API Documentation & Outputs

### Public Health Check
Retrieves the application running status without requiring authentication.
- **Method**: `GET`
- **URL**: `/api/health`
- **Headers**: *(None)*
- **Sample Output (200 OK)**:
```json
{
  "status": "UP"
}
```

### 1. Get All Active Restaurants
Retrieves a list of all active restaurants. Available to roles: `CUSTOMER`, `RESTAURANT`, `RIDER`.
- **Method**: `GET`
- **URL**: `/api/restaurants`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Sample Output (200 OK)**:
```json
[
  {
    "restaurantId": "d09436fe-6d04-4df8-8547-8149f7e52a92",
    "name": "The Spice Route",
    "rating": 4.85,
    "ratingCount": 150,
    "description": "Authentic Indian cuisine with rich flavours and premium ambience.",
    "country": "India",
    "state": "Karnataka",
    "district": "Bangalore",
    "city": "Bengaluru",
    "doorNo": "12/A, 100 Feet Road",
    "mobileNumber": "+919876543210",
    "openTime": "11:00:00",
    "closeTime": "23:00:00",
    "createdAt": "2026-06-13T21:40:00",
    "updatedAt": "2026-06-13T21:40:00",
    "imageUrl": "https://example.com/images/spiceroute.jpg",
    "active": true
  }
]
```

### 2. Get Restaurant by ID
Retrieves details of a specific restaurant by its UUID. Available to roles: `CUSTOMER`, `RESTAURANT`, `RIDER`.
- **Method**: `GET`
- **URL**: `/api/restaurants/{id}`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Sample Output (200 OK)**:
```json
{
  "restaurantId": "d09436fe-6d04-4df8-8547-8149f7e52a92",
  "name": "The Spice Route",
  "rating": 4.85,
  "ratingCount": 150,
  "description": "Authentic Indian cuisine with rich flavours and premium ambience.",
  "country": "India",
  "state": "Karnataka",
  "district": "Bangalore",
  "city": "Bengaluru",
  "doorNo": "12/A, 100 Feet Road",
  "mobileNumber": "+919876543210",
  "openTime": "11:00:00",
  "closeTime": "23:00:00",
  "createdAt": "2026-06-13T21:40:00",
  "updatedAt": "2026-06-13T21:40:00",
  "imageUrl": "https://example.com/images/spiceroute.jpg",
  "active": true
}
```

### 3. Create Restaurant
Creates a new restaurant. **Restricted to role: `RESTAURANT`**.
- **Method**: `POST`
- **URL**: `/api/restaurants`
- **Headers**:
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Sample Request Body**:
```json
{
  "name": "Gourmet Garden",
  "rating": 4.50,
  "ratingCount": 80,
  "description": "Fresh, locally-sourced farm-to-table salads and entrees.",
  "country": "India",
  "state": "Tamil Nadu",
  "district": "Chennai",
  "city": "Chennai",
  "doorNo": "45, Gandhi Nagar First Main",
  "mobileNumber": "+919123456789",
  "openTime": "09:00:00",
  "closeTime": "22:00:00",
  "imageUrl": "https://example.com/images/gourmet.jpg"
}
```
- **Sample Output (217 Created)**:
```json
{
  "restaurantId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "name": "Gourmet Garden",
  "rating": 4.5,
  "ratingCount": 80,
  "description": "Fresh, locally-sourced farm-to-table salads and entrees.",
  "country": "India",
  "state": "Tamil Nadu",
  "district": "Chennai",
  "city": "Chennai",
  "doorNo": "45, Gandhi Nagar First Main",
  "mobileNumber": "+919123456789",
  "openTime": "09:00:00",
  "closeTime": "22:00:00",
  "createdAt": "2026-06-13T21:45:10.123",
  "updatedAt": "2026-06-13T21:45:10.123",
  "imageUrl": "https://example.com/images/gourmet.jpg",
  "active": true
}
```

### 4. Update Restaurant
Updates details of an existing restaurant by UUID. **Restricted to role: `RESTAURANT`**.
- **Method**: `PUT`
- **URL**: `/api/restaurants/{id}`
- **Headers**:
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Sample Request Body**:
```json
{
  "name": "Gourmet Garden Cafe",
  "rating": 4.65,
  "ratingCount": 92,
  "description": "Fresh, organic farm-to-table salads, coffees and healthy desserts.",
  "country": "India",
  "state": "Tamil Nadu",
  "district": "Chennai",
  "city": "Chennai",
  "doorNo": "45, Gandhi Nagar First Main",
  "mobileNumber": "+919123456789",
  "openTime": "08:30:00",
  "closeTime": "22:30:00",
  "imageUrl": "https://example.com/images/gourmet-cafe.jpg",
  "active": true
}
```
- **Sample Output (200 OK)**:
```json
{
  "restaurantId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "name": "Gourmet Garden Cafe",
  "rating": 4.65,
  "ratingCount": 92,
  "description": "Fresh, organic farm-to-table salads, coffees and healthy desserts.",
  "country": "India",
  "state": "Tamil Nadu",
  "district": "Chennai",
  "city": "Chennai",
  "doorNo": "45, Gandhi Nagar First Main",
  "mobileNumber": "+919123456789",
  "openTime": "08:30:00",
  "closeTime": "22:30:00",
  "createdAt": "2026-06-13T21:45:10.123",
  "updatedAt": "2026-06-13T21:48:05.456",
  "imageUrl": "https://example.com/images/gourmet-cafe.jpg",
  "active": true
}
```

### 5. Delete Restaurant (Soft Delete)
Marks the restaurant as inactive (soft delete) instead of dropping the record from the database. **Restricted to role: `RESTAURANT`**.
- **Method**: `DELETE`
- **URL**: `/api/restaurants/{id}`
- **Headers**:
  - `Authorization: Bearer <token>`
- **Sample Output (204 No Content)**:
*(No response body returned)*

---

## Global Exception Handling

A centralized exception handler returns consistent, structured JSON responses for errors.

### Error Response Schema
```json
{
  "timestamp": "ISO-8601 formatted timestamp",
  "status": 500,
  "error": "HTTP Status Reason Phrase",
  "message": "Detailed error message explanation",
  "path": "Requested URI path"
}
```

### Sample Exception Outputs

#### A. Resource Not Found (404 Not Found)
Occurs when requesting, updating, or deleting a UUID that does not exist in the database.
- **Sample Request**: `GET http://localhost:8080/api/restaurants/00000000-0000-0000-0000-000000000000`
- **Response**:
```json
{
  "timestamp": "2026-06-13T21:50:12.789",
  "status": 404,
  "error": "Not Found",
  "message": "Restaurant not found with id: 00000000-0000-0000-0000-000000000000",
  "path": "/api/restaurants/00000000-0000-0000-0000-000000000000"
}
```

#### B. Parameter Type Mismatch (400 Bad Request)
Occurs when providing an invalid UUID format in the path variable.
- **Sample Request**: `GET http://localhost:8080/api/restaurants/invalid-uuid-string`
- **Response**:
```json
{
  "timestamp": "2026-06-13T21:52:03.111",
  "status": 400,
  "error": "Bad Request",
  "message": "Parameter 'id' should be of type 'UUID'",
  "path": "/api/restaurants/invalid-uuid-string"
}
```

#### C. Unauthorized access (401 Unauthorized)
Occurs when sending requests without a JWT token or with an invalid/expired token.
- **Sample Request**: `GET http://localhost:8080/api/restaurants` *(No Auth Header)*
- **Response**: Standard Spring Security `401 Unauthorized` response.

#### D. Forbidden Access (403 Forbidden)
Occurs when an authenticated user attempts to access an endpoint for which their role lacks permission (e.g. a `CUSTOMER` trying to create a restaurant).
- **Sample Request**: `POST http://localhost:8080/api/restaurants` *(With a CUSTOMER token)*
- **Response**: Standard Spring Security `403 Forbidden` response.
