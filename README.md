# Swigg Backend API

A modern, scalable food delivery platform backend built with Spring Boot 3.3, featuring async processing, distributed caching, and event-driven architecture.

## 🚀 Features

- **Async OTP Generation & Delivery**: Non-blocking OTP generation with RabbitMQ message queues
- **Geocoding Service**: Address validation and reverse geocoding with Redis caching
- **Distributed Caching**: Cache-aside pattern using Redis for improved performance
- **Message-Driven Architecture**: RabbitMQ for OTP, Geocoding, and Logging events
- **Database Optimization**: Strategic indexing on high-frequency query columns
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Support for User, Customer, Restaurant, and Rider roles
- **PostgreSQL Database**: Reliable relational data storage

## ⚡ Quick Start

### Using Docker Compose (Recommended)

**Linux/macOS:**
```bash
chmod +x start.sh
./start.sh
```

**Windows:**
```bash
start.bat
```

**Application URL:** `http://localhost:8080/swigg`
**RabbitMQ UI:** `http://localhost:15672` (guest/guest)

## 🏗️ Architecture

```
Client → Spring Boot API → PostgreSQL (Data)
                      ↓
                   Redis (Cache)
                      ↓
                   RabbitMQ (Queues)
                      ↓
                  Message Listeners
```

## ⚙️ Configuration

All configuration is in `src/main/resources/application.properties`

### Key Settings

```properties
# Redis Caching
cache.default-ttl=3600          # 1 hour
cache.geocoding-ttl=3600        # 1 hour
cache.otp-ttl=300               # 5 minutes

# RabbitMQ Messaging
rabbitmq.otp.queue=otp.queue
rabbitmq.geocoding.queue=geocoding.queue
rabbitmq.logging.queue=logging.queue

# TOTP Settings
totp.window=5                   # ~5 minutes
totp.time-step=30               # 30 seconds per step
```

### Environment Profiles

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production  
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## 📡 API Endpoints

### Authentication
- `POST /api/users/signup/request` - Sign up with OTP
- `POST /api/users/signup/verify` - Verify signup
- `POST /api/users/login/request` - Request login OTP
- `POST /api/users/login/verify` - Verify login

### Customer
- `POST /api/customers/register/request` - Register (Async)
- `POST /api/customers/login/request` - Login (Async)
- `PUT /api/customers/update` - Update profile (Async geocoding)
- `GET /api/customers/{id}` - Get details

### Restaurant
- `POST /api/restaurants/register/request` - Register (Async)
- `POST /api/restaurants/login/request` - Login (Async)
- `PUT /api/restaurants/update` - Update (Async geocoding)
- `GET /api/restaurants/{id}` - Get details

### Rider
- `POST /api/riders/register/request` - Register (Async)
- `POST /api/riders/login/request` - Login (Async)
- `PUT /api/riders/update` - Update (Async geocoding)
- `GET /api/riders/{id}` - Get details

## 📚 Documentation

- **[SETUP_GUIDE.md](./SETUP_GUIDE.md)** - Complete setup and deployment guide
- **[docker-compose.yml](./docker-compose.yml)** - Docker services configuration

## 🔧 Development

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Build
```bash
mvn clean install -DskipTests
```

### Run Tests
```bash
mvn test
```

### Project Structure
```
src/main/java/com/swigg/
├── auth/                # JWT & TOTP
├── cache/               # Redis caching
├── config/              # Spring configs
├── customer/            # Customer module
├── geocoding/           # Geocoding service
├── messaging/           # SMS & events
├── restaurant/          # Restaurant module
├── rider/               # Rider module
└── user/                # User module
```

## 🚀 Deployment

### Docker
```bash
docker build -t swigg-app:1.0 .
docker run -d -p 8080:8080 --env-file .env.prod swigg-app:1.0
```

### Kubernetes
```bash
kubectl create configmap swigg-config --from-file=application.properties
kubectl apply -f k8s/deployment.yaml
```

## 🐛 Troubleshooting

### Services Not Starting
```bash
# Check if containers are running
docker-compose ps

# Restart services
docker-compose restart

# View logs
docker-compose logs -f <service-name>
```

### Database Connection Error
```bash
# Verify PostgreSQL
psql -h localhost -U postgres -d swigg_db

# Restart PostgreSQL
docker restart swigg-postgres
```

### Port Already in Use
```bash
# Change port in application.properties
server.port=8081
```

See [SETUP_GUIDE.md](./SETUP_GUIDE.md#troubleshooting) for detailed troubleshooting.

## 📊 Monitoring

- **Health Check**: `GET /swigg/actuator/health`
- **Metrics**: `GET /swigg/actuator/metrics`
- **RabbitMQ UI**: http://localhost:15672
- **Application Logs**: `logs/app.log`

## 🔐 Security

- JWT tokens: 24-hour expiration
- TOTP window: 5 minutes
- Password hashing: bcrypt
- SQL injection prevention: Parameterized queries

## 📝 Environment Variables (Production)

```bash
# Database
DB_URL=jdbc:postgresql://postgres:5432/swigg_db
DB_USER=postgres
DB_PASSWORD=<secure-password>

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<secure-password>

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=<secure-password>

# JWT
JWT_SECRET=<secure-secret>

# Twilio (SMS)
TWILIO_ACCOUNT_SID=<your-sid>
TWILIO_AUTH_TOKEN=<your-token>

# Google Maps
GOOGLE_MAPS_API_KEY=<your-key>
```

## 📌 Version

- **Current**: 1.0.0
- **Updated**: 2026-06-15
- **Java**: 21+
- **Spring Boot**: 3.3.0

---

For detailed setup instructions, see [SETUP_GUIDE.md](./SETUP_GUIDE.md)

**Happy Coding! 🎉**
