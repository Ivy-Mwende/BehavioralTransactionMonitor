# System Architecture

## High-Level Overview
┌─────────────┐
│ REST API │ ← Client requests
└──────┬──────┘
│
▼
┌──────────────────────────────────┐
│ Spring Boot Application │
│ ┌────────────────────────────┐ │
│ │ TransactionController │ │
│ │ - Health check │ │
│ │ - Ingest transactions │ │
│ │ - Query profiles │ │
│ └────────────┬───────────────┘ │
│ │ │
│ ┌────────────▼───────────────┐ │
│ │ Kafka Producer Service │ │
│ │ - Send to transactions- │ │
│ │ stream topic │ │
│ └────────────┬───────────────┘ │
│ │ │
└───────────────┼───────────────────┘
│
┌────────▼────────┐
│ Apache Kafka │
│ Topic: txn- │
│ stream │
└────────┬────────┘
│
┌────────▼──────────────┐
│ Kafka Consumer │
│ Service │
└────────┬──────────────┘
│
┌────────▼──────────────┐
│ PostgreSQL DB │
│ - users │
│ - transactions │
│ - user_profiles │
└───────────────────────┘
┌────────────────────────────────┐
│ Caching Layer (Redis) │
│ - User profiles (1h TTL) │
│ - Risk scores (1h TTL) │
└────────────────────────────────┘

## Data Flow

1. **Transaction Ingestion**
    - Client sends POST `/api/v1/transactions/ingest`
    - TransactionController receives request
    - TransactionProducerService sends to Kafka
    - Immediate 202 response

2. **Event Processing**
    - Kafka Consumer listens on `transactions-stream`
    - Deserializes TransactionEvent
    - Saves to PostgreSQL transactions table
    - Logs completion

3. **Caching Strategy**
    - User profiles cached in Redis (1 hour TTL)
    - Risk scores cached (1 hour TTL)
    - Cache evicted on profile update
    - Cache miss falls back to database

## Technology Dependencies

| Component | Purpose | Version |
|-----------|---------|---------|
| Spring Boot | Application framework | 4.1.1 |
| PostgreSQL | Primary database | 16 |
| Redis | In-memory cache | 7 |
| Kafka | Event streaming | 7.5.0 |
| JGraphT | Graph algorithms | (Phase 3) |
| Jackson | JSON serialization | 2.15+ |
| Lombok | Code generation | 1.18+ |

## Configuration Profiles

### Development
- DDL: `update` (auto-migrate)
- Logging: DEBUG
- Swagger: Enabled
- Cache: In-memory or Redis

### Testing
- Database: H2 in-memory
- DDL: `create-drop` (fresh each run)
- Logging: WARN
- Cache: Simple (not Redis)
- Swagger: Disabled

### Production
- DDL: `validate` (no changes)
- Logging: INFO
- Swagger: Disabled
- Cache: Redis with cluster support
- Database: PostgreSQL with connection pooling
- HTTPS: Enabled

## Deployment Architecture (Future)
┌──────────────────────────────┐
│ Load Balancer (NGINX) │
└──────────────┬───────────────┘
│
┌──────────┼──────────┐
│ │ │
▼ ▼ ▼
┌────────┐ ┌────────┐ ┌────────┐
│ BTM │ │ BTM │ │ BTM │
│ Pod 1 │ │ Pod 2 │ │ Pod 3 │
└────┬───┘ └────┬───┘ └────┬───┘
│ │ │
└──────────┼──────────┘
│
┌──────────┴──────────┐
│ │
▼ ▼
┌────────────┐ ┌──────────┐
│ PostgreSQL │ │ Redis │
│ Primary │ │ Cluster │
└────────────┘ └──────────┘


---

**Phase 1 Complete**: Core infrastructure ready for behavioral detection implementation.
