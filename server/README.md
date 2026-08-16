# Receiptly Server

## Prerequisites

- JDK 21
- Docker & Docker Compose
- Gradle

## Setup

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Run migrations

```bash
docker exec -it receiptly-server-postgres-1 psql -U postgres -d receiptly -f /docker-entrypoint-initdb.d/V1__create_receipts_table.sql
```

Or manually:

```bash
docker exec -it receiptly-server-postgres-1 psql -U postgres -d receiptly
```

Then paste the SQL from `app/src/main/resources/db/migration/V1__create_receipts_table.sql`.

### 3. Verify table exists

```bash
docker exec -it receiptly-server-postgres-1 psql -U postgres -d receiptly -c "\dt"
```

### 4. Run the application

```bash
./gradlew :app:run
```

## Environment Variables

Create a `.env` file in the server root:

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
TELEGRAM_BOT_TOKEN=your_bot_token
```
