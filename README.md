# Banking Transaction Processor

A Spring Boot service that processes banking transactions for multiple accounts. Each account has a unique ID, a balance, and a timestamped transaction ledger. The API supports deposits, withdrawals, transfers, balance queries, and transaction history.

## Tech Stack

| Layer        | Technology              |
|-------------|-------------------------|
| Language    | Java 17                 |
| Framework   | Spring Boot 3.2         |
| Build       | Maven                   |
| Database    | H2 (in-memory, auto-configured on startup) |
| API docs    | Swagger UI (springdoc-openapi)               |
| API style   | REST + JSON                                  |

## Features

- **Accounts** — Unique UUID, holder name, non-negative balance, optimistic locking (`version`)
- **Operations** — Deposit, withdrawal, transfer between accounts
- **Validation** — Rejects zero/negative amounts, overdrafts, same-account transfers, missing accounts
- **Ledger** — Every monetary operation recorded with type, amount, balance-after, counterparty (for transfers), description, and timestamp
- **Queries** — Account balance and full transaction history (newest first)

## Project Structure

```
src/main/java/com/wealthcdio/banking/
├── controller/          REST endpoints
├── service/             Business logic (AccountService, LedgerService, TransactionValidator)
├── repository/          Spring Data JPA
├── model/               Account, Transaction entities
├── dto/                 Request/response objects
└── exception/           Domain errors and global handler

schema/requirements-schema.sql   H2 DDL reference (tables auto-created by JPA)
postman/                         Postman collection for manual testing
```

## Prerequisites

- **JDK 17+**
- **Maven 3.9+**

No external database installation is required. H2 is embedded and configured automatically when the application starts.

## Database (H2)

On startup, Spring Boot:

1. Starts an in-memory H2 database (`banking_db`)
2. Creates/updates `accounts` and `transactions` tables from JPA entities (`ddl-auto: update`)

**H2 Console** (optional): http://localhost:8080/h2-console

| Setting | Value |
|---------|--------|
| JDBC URL | `jdbc:h2:mem:banking_db` |
| User | `sa` |
| Password | *(empty)* |

Data is reset when the application stops (in-memory). See `schema/requirements-schema.sql` for the reference DDL.

## Build and Run

### Clean build and run all tests

```bash
mvn clean test
```

Tests use the `test` profile with a separate in-memory H2 instance.

### Run the application

```bash
mvn spring-boot:run
```

The API listens on **http://localhost:8080**.

### Swagger UI

Open **http://localhost:8080/swagger-ui.html** to explore and execute all endpoints interactively.

OpenAPI JSON: **http://localhost:8080/api-docs**

### Package JAR

```bash
mvn clean package -DskipTests
java -jar target/banking-transaction-processor-1.0.0-SNAPSHOT.jar
```

## Configuration

H2 settings in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:banking_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
```

`ddl-auto: update` creates or updates tables automatically—no manual schema script is required to run locally.

## API Reference

Base path: `/api/accounts`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/accounts` | Create account |
| `GET` | `/api/accounts/{accountId}` | Get balance and account details |
| `PUT` | `/api/accounts/{accountId}` | Update account holder name |
| `POST` | `/api/accounts/{accountId}/deposit` | Deposit funds |
| `POST` | `/api/accounts/{accountId}/withdraw` | Withdraw funds |
| `POST` | `/api/accounts/{accountId}/transfer` | Transfer to another account |
| `GET` | `/api/accounts/{accountId}/transactions` | Transaction history (newest first) |

### Error responses

Errors use [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807):

| `title` (ErrorCode) | HTTP | When |
|---------------------|------|------|
| `ACCOUNT_NOT_FOUND` | 404 | Unknown account ID |
| `INSUFFICIENT_FUNDS` | 400 | Withdrawal/transfer exceeds balance |
| `INVALID_AMOUNT` | 400 | Amount ≤ 0 (service layer) |
| `SAME_ACCOUNT_TRANSFER` | 400 | Transfer source = destination |
| `VALIDATION_ERROR` | 400 | Bean validation on request body |
