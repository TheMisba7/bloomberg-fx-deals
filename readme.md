# FX Deals Data Warehouse

A production-ready Spring Boot application for Bloomberg to import, validate, and persist foreign exchange (FX) deal data. Built with enterprise-grade features including comprehensive validation, duplicate prevention, error tracking, and AOP-based cross-cutting concerns.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Validation Rules](#validation-rules)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Error Handling](#error-handling)
- [Development](#development)
- [Deployment](#deployment)

---

## ✨ Features

### Core Functionality
- ✅ **Single Deal Import**: Import individual FX deals via REST API
- ✅ **CSV Batch Import**: Upload CSV files with multiple deals
- ✅ **Comprehensive Validation**: ISO 4217 currency codes, amount validation, timestamp checks
- ✅ **Duplicate Prevention**: Business-key based deduplication (dealId)
- ✅ **No Rollback Design**: Independent transaction per deal (REQUIRES_NEW propagation)
- ✅ **Error Tracking**: All import errors persisted to database with categorization

### Enterprise Features
- ✅ **AOP-Based Validation**: Separation of concerns using Spring AOP
- ✅ **Liquibase Migrations**: Database version control
- ✅ **Docker Support**: Full containerization with Docker Compose
- ✅ **High Test Coverage**: 80%+ code coverage with JUnit 5 & Mockito
- ✅ **Production Logging**: Structured logging with SLF4J/Logback
- ✅ **RESTful API**: Clean REST endpoints with proper HTTP status codes

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     REST API Layer                          │
│                   (FxDealApi)                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Application Layer                          │
│                   (FxDealApp)                               │
│  - Orchestrates batch processing                           │
│  - Handles error aggregation                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   AOP Aspect Layer                          │
│              (FxDealImportAspect)                           │
│  - Fx deal validation                                       │
│  - Logging (before/after/error)                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│    (FxDealService, ErrorService)                            │
│  - Business logic                                           │
│  - Transaction management                                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                 Data Access Layer                           │
│         (FxDealDao, ImportErrorTypeDao)                     │
│  - JPA repositories                                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL 15                            │
│  - fx_deals table                                           │
│  - import_errors table                                      │
└─────────────────────────────────────────────────────────────┘
```

### Key Design Decisions

1. **Independent Transactions**: Each deal uses `@Transactional(propagation = Propagation.REQUIRES_NEW)` to ensure no rollback across deals
2. **AOP for Cross-Cutting Concerns**: Validation and logging separated from business logic using Spring AOP
3. **Error Persistence**: All failures tracked in separate `import_errors` table for audit and analysis
4. **Idempotency**: Duplicate detection prevents accidental re-imports based on `dealId`

---

## 🛠 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.5.7 |
| **Database** | PostgreSQL | 15 |
| **ORM** | Hibernate/JPA | 6.x |
| **Migration** | Liquibase | Latest |
| **Build Tool** | Maven | 3.x |
| **Testing** | JUnit 5 + Mockito | Latest |
| **Coverage** | JaCoCo | 0.8.12 |
| **Containerization** | Docker + Docker Compose | Latest |
| **CSV Parsing** | OpenCSV | Latest |

---

## 📦 Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Maven** (or use provided `mvnw`)
- **curl** and **jq** (optional, for testing)

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/TheMisba7/bloomberg-fx-deals.git
cd bloomberg-fx-deals
```

### 2. Build the Application

```bash
# Using Maven wrapper (recommended)
./mvnw clean package -DskipTests

# Or using Make
make build
```

### 3. Start Services with Docker Compose

```bash
# Start PostgreSQL and Application
make run

# Or manually
docker-compose up --build -d
```

### 4. Verify Installation

```bash
# Check containers are running
docker-compose ps

# View logs
make logs

# Test API (should return empty array initially)
curl http://localhost:8080/fx-deals
```

### 5. Upload Sample Data

```bash
make upload-sample
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/fx-deals
```

### Endpoints

#### 1. Import Single Deal

**POST** `/fx-deals`

**Request Body:**
```json
{
  "dealId": "DEAL-001",
  "currencyFrom": "USD",
  "currencyTo": "EUR",
  "dealTimestamp": "2024-01-15T10:30:00",
  "dealAmount": 10000.50,
  "exchangeRate": 0.85
}
```

**Success Response (201 Created):**
```json
{
  "totalRecords": 1,
  "successfulImports": 1,
  "failedImports": 0,
  "duplicateImports": 0,
  "errors": []
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-11-19T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "From currency 'XXX' is not a valid ISO 4217 currency code"
}
```

#### 2. Upload CSV File

**POST** `/fx-deals/upload`

**Content-Type:** `multipart/form-data`

**Form Parameter:** `file` (CSV file)

**CSV Format:**
```csv
dealUniqueId,fromCurrency,toCurrency,dealTimestamp,dealAmount,exchangerate
DEAL-001,USD,EUR,2024-01-15T10:30:00,10000.50,0.85
DEAL-002,GBP,USD,2024-01-15T11:45:00,25000.75,1.27
```

**Example:**
```bash
curl -X POST -F "file=@sample-data/sample-deals.csv" \
  http://localhost:8080/fx-deals/upload
```

**Partial Success Response (201 Created):**
```json
{
  "totalRecords": 10,
  "successfulImports": 8,
  "failedImports": 1,
  "duplicateImports": 1,
  "errors": [
    {
      "id": 1,
      "rowNumber": 3,
      "dealId": "DEAL-003",
      "errorMessage": "Deal with ID 'DEAL-003' already exists",
      "errorType": "DUPLICATE",
      "createdAt": "2024-11-19T10:35:00"
    },
    {
      "id": 2,
      "rowNumber": 7,
      "dealId": "DEAL-007",
      "errorMessage": "From currency 'XXX' is not a valid ISO 4217 currency code",
      "errorType": "VALIDATION",
      "createdAt": "2024-11-19T10:35:01"
    }
  ]
}
```

---

## 🗄 Database Schema

### fx_deals Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Surrogate key |
| deal_id | VARCHAR(255) | NOT NULL, UNIQUE | Business unique identifier |
| currency_from | VARCHAR(3) | NOT NULL | Ordering currency (ISO 4217) |
| currency_to | VARCHAR(3) | NOT NULL | Target currency (ISO 4217) |
| deal_timestamp | TIMESTAMP | NOT NULL | Deal execution time |
| deal_amount | DECIMAL(19,4) | NOT NULL | Amount in ordering currency |
| exchange_rate | DOUBLE | NOT NULL | Exchange rate at deal time |
| created_at | TIMESTAMP | NOT NULL | Record creation timestamp |

**Indexes:**
- `idx_deal_id` on `deal_id` (for duplicate checks)
- `idx_deal_timestamp` on `deal_timestamp` (for time-based queries)

### import_errors Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Error record ID |
| row_number | INT | | Source file row number |
| deal_id | VARCHAR(255) | | Failed deal identifier |
| error_message | TEXT | NOT NULL | Error description |
| error_type | VARCHAR(50) | NOT NULL | Error category (VALIDATION, DUPLICATE, UNKNOWN) |
| created_at | TIMESTAMP | NOT NULL | Error log timestamp |

---


---

## 📁 Project Structure

```
bloomberg-datawarehouse/
├── src/
│   ├── main/
│   │   ├── java/org/boolmberg/datawarehouse/
│   │   │   ├── api/                    # REST Controllers
│   │   │   │   ├── FxDealApi.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── app/                    # Application Layer
│   │   │   │   └── FxDealApp.java
│   │   │   ├── aspect/                 # AOP Aspects
│   │   │   │   └── FxDealImportAspect.java
│   │   │   ├── dao/                    # JPA Repositories
│   │   │   │   ├── FxDealDao.java
│   │   │   │   └── ImportErrorTypeDao.java
│   │   │   ├── dto/                    # Data Transfer Objects
│   │   │   │   ├── FxDealDTO.java
│   │   │   │   └── ImportSummary.java
│   │   │   ├── exception/              # Custom Exceptions
│   │   │   │   ├── DuplicateDealException.java
│   │   │   │   ├── FxDealNotFoundException.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   └── InvalidFileException.java
│   │   │   ├── model/                  # JPA Entities
│   │   │   │   ├── FxDeal.java
│   │   │   │   ├── ImportError.java
│   │   │   │   └── ImportErrorType.java
│   │   │   ├── service/                # Business Logic
│   │   │   │   ├── FxDealService.java
│   │   │   │   └── ErrorService.java
│   │   │   ├── utils/                  # Utility Classes
│   │   │   │   └── FileUtils.java
│   │   │   ├── validator/              # Validation Logic
│   │   │   │   └── FxDealValidator.java
│   │   │   └── BloombergDatawarehouseApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-test.yml
│   │       └── db/changelog/
│   │           └── tables-log.yml      # Liquibase migrations
│   └── test/
│       └── java/org/boolmberg/datawarehouse/
│           ├── FxDealAppTest.java
│           └── BloombergDatawarehouseApplicationTests.java
├── sample-data/
│   └── sample-deals.csv                # Sample CSV file
├── docker-compose.yml
├── Dockerfile
├── Makefile
├── pom.xml
└── README.md
```

---

## ⚙️ Configuration

### Application Properties

Located in `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/fx_deals
spring.datasource.username=postgres
spring.datasource.password=whynot

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# Liquibase
spring.liquibase.change-log=classpath:db/changelog/tables-log.yml
spring.liquibase.enabled=true

# Logging
logging.level.root=INFO
logging.level.com.bloomberg.fxdeals=DEBUG

# Server
server.port=8080
```

### Environment Variables

Override defaults using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fx_deals
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=secure_password
```

### Docker Compose Configuration

```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: fx_deals
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: whynot
    ports:
      - "5432:5432"

  fx-deals-app:
    build: .
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/fx_deals
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: whynot
    ports:
      - "8080:8080"
```

---

## 🚨 Error Handling

### Error Types

| Type | HTTP Status | Description |
|------|-------------|-------------|
| **VALIDATION** | 400 Bad Request | Field validation failures |
| **DUPLICATE** | 400 Bad Request | Deal ID already exists |
| **UNKNOWN** | 500 Internal Server Error | Unexpected errors |

### Error Response Format

```json
{
  "timestamp": "2024-11-19T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "From currency 'XXX' is not a valid ISO 4217 currency code",
  "validationErrors": {
    "currencyFrom": "From currency must be a valid 3-letter ISO code"
  }
}
```

## 💻 Development

### Make Commands

```bash
make help              # Show all available commands
make build             # Build application
make test              # Run tests with coverage
make run               # Start with Docker Compose
make stop              # Stop containers
make logs              # View logs
make clean             # Clean everything
make upload-sample     # Upload sample CSV
make restart           # Restart application
make full-deploy       # Clean, build, and deploy
```