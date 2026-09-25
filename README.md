# Mini Banking API - Week 2 and Week 3

A Spring Boot REST API for customers, bank accounts, account transactions, and beneficiaries. Spring Data JPA and Hibernate persist the domain entities to PostgreSQL; tests use H2.

## Prerequisites

- Java 17 or later
- Maven 3.9 or later
- Docker Desktop for PostgreSQL / Compose
- Postman (optional)

## Run tests

```powershell
mvn test
```

Tests use an in-memory H2 database and cover customer/account creation, deposits, transaction history, insufficient funds, validation, and beneficiary create/list/delete.

## Run with PostgreSQL

Start PostgreSQL and the application locally:

```powershell
docker compose up -d postgres
mvn spring-boot:run
```

Or run the complete Compose stack:

```powershell
docker compose up --build
```

The default connection is `jdbc:postgresql://localhost:5432/banking` with database/user/password `banking`. Hibernate creates or updates the mapped tables on startup. Configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` to use different credentials. These defaults are for local learning only.

## API

All successful create requests return `201 Created`; deleting a beneficiary returns `204 No Content`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/customers` | Create a customer |
| `GET` | `/api/customers` | List customers |
| `GET` | `/api/customers/{id}` | Get a customer |
| `POST` | `/api/accounts` | Create an account for a customer |
| `GET` | `/api/accounts` | List accounts |
| `GET` | `/api/accounts/{accountId}` | Get an account and current balance |
| `POST` | `/api/accounts/{accountId}/transactions` | Deposit or withdraw |
| `GET` | `/api/accounts/{accountId}/transactions` | List account transactions |
| `POST` | `/api/beneficiaries` | Add a beneficiary for a customer |
| `GET` | `/api/beneficiaries` | List beneficiaries |
| `DELETE` | `/api/beneficiaries/{id}` | Delete a beneficiary |

Create customer body:

```json
{"fullName":"Ada Lovelace","email":"ada@example.com","phone":"555-0100"}
```

Create account body:

```json
{"customerId":1,"initialBalance":100.00}
```

Transaction body (`type` is `DEPOSIT` or `WITHDRAWAL`):

```json
{"type":"DEPOSIT","amount":25.00,"description":"Initial deposit"}
```

Beneficiary body:

```json
{"customerId":1,"name":"Grace Hopper","accountNumber":"1234567890","bankName":"Example Bank"}
```

Amounts must be positive for transactions and use at most two decimal places. Withdrawals that would make the balance negative return `422 Unprocessable Entity`. Invalid requests return `400`; unknown resources return `404`; duplicate customer email returns `409`. Error responses include `status`, `message`, `path`, and field-level `validationErrors` when applicable.

## Postman

Import `postman/mini-banking-week2.postman_collection.json`. The create-customer, create-account, and create-beneficiary requests save generated IDs into collection variables. Run those requests before requests that use the IDs.

## Persistence model

- `customers` stores validated profile data; email is unique.
- `bank_accounts` belongs to a customer and stores the current decimal balance.
- `account_transactions` records immutable deposit/withdrawal entries linked to an account.
- `beneficiaries` belongs to a customer.

Transactions run atomically and lock the account row while changing its balance. The `schema.sql` file notes that Hibernate currently manages this exercise's schema; a production deployment should use versioned migrations and externally managed secrets.

## Week 3 frontend

The frontend is plain JavaScript served by Spring Boot from `src/main/resources/static`. It uses the same origin as the API, so no CORS configuration or separate frontend server is needed. The API base URL is configured in `static/js/config.js` using `window.location.origin`; when deployed on another host, the browser automatically uses that host's origin.

Start PostgreSQL and Spring Boot using the commands above, then open `http://localhost:8080/`. The dashboard supports:

- Customer list, detail lookup, and customer creation.
- Account list, account creation, account details, transaction history, deposits, and withdrawals.
- Beneficiary list, creation, and deletion.
- API connection status, refresh, client-side form constraints, server validation messages, and request failure messages.

The UI displays monetary values to two decimal places without assuming a currency, since the API does not currently model one. Browser-side checks improve form feedback; the backend remains responsible for authoritative validation.

## Git discipline

Review changes before committing and keep commits focused:

```powershell
git status
git diff
git add src pom.xml README.md postman
git commit -m "Add Week 2 banking CRUD APIs"
```
