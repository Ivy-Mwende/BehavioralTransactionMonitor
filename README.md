# BehavioralTransactionMonitor

A real-time fraud detection system using behavioral profiling and transaction graph analysis.

## Overview

BehavioralTransactionMonitor detects fraudulent transactions by:
1. **Behavioral Anomaly Detection** - Learns individual user spending patterns and flags deviations
2. **Fraud Ring Detection** - Identifies organized fraud through transaction graph analysis (Phase 3)
3. **Real-time Risk Scoring** - <100ms latency fraud risk assessment

## Technology Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 4.1.1
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Messaging**: Apache Kafka 7.5.0
- **Graph DB**: Neo4j (Phase 3)
- **Build**: Maven 3.9+
- **API Docs**: Swagger/OpenAPI 3.0

## Project Structure
BehavioralTransactionMonitor/
├── src/main/java/com/fintech/btm/
│ ├── api/ # REST controllers
│ ├── config/ # Spring configurations
│ ├── dto/ # Data transfer objects
│ ├── model/ # JPA entities
│ ├── repository/ # Data access layer
│ └── service/ # Business logic
├── src/main/resources/
│ ├── application.yml # Base configuration
│ ├── application-dev.yml
│ ├── application-test.yml
│ └── application-prod.yml
├── docker-compose.yml # Local development stack
├── pom.xml # Maven dependencies
└── README.md


## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Git

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/Ivy-Mwende/BehavioralTransactionMonitor.git
cd BehavioralTransactionMonitor
```

2. **Start infrastructure**
```bash
docker-compose up -d
```

Verify all containers are running:
```bash
docker-compose ps
```

3. **Build the application**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### Configuration

#### Development (Default)
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

#### Testing
```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

#### Production
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## API Documentation

Once the app is running, view interactive API docs:
http://localhost:8080/api/swagger-ui.html

## API Endpoints

### Health Check
```bash
GET /api/v1/transactions/health
```

### Ingest Transaction
```bash
POST /api/v1/transactions/ingest
Content-Type: application/json

{
  "transactionId": 1001,
  "userId": 1,
  "amount": 250.50,
  "merchantCategory": "GROCERY",
  "merchantName": "Carrefour Supermarket",
  "locationLatitude": -1.2866,
  "locationLongitude": 36.8172,
  "transactionTimestamp": "2026-09-04T14:00:00"
}
```

### Test Transaction
```bash
POST /api/v1/transactions/test
```

### Get User Profile
```bash
GET /api/v1/transactions/{userId}/profile
```

### Test Caching
```bash
POST /api/v1/transactions/cache-test
```

## Development

### Running Tests
```bash
mvn test
```

### Building Docker Image
```bash
mvn clean package
docker build -t btm:latest .
```

### Database Migrations
```bash
docker exec -it btm-postgres psql -U btm_user -d btm_db
```

## Environment Variables (Production)

```bash
DB_HOST=postgres.example.com
DB_PORT=5432
DB_NAME=btm_db
DB_USER=btm_user
DB_PASSWORD=secure_password

REDIS_HOST=redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000

APP_USER=admin
APP_PASSWORD=strong_password

SSL_ENABLED=true
SSL_KEYSTORE=/etc/ssl/keystore.p12
SSL_KEYSTORE_PASSWORD=keystore_password


## Author

**Ivy Mwende**
- GitHub: [@Ivy-Mwende](https://github.com/Ivy-Mwende)
- Email: mwendeivymumbi@gmail.com
- Location: Nairobi, Kenya


