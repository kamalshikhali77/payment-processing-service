# Payment Processing Service

A RESTful payment-processing microservice built with Grails 5.3.6 and GORM.

The service allows merchants to create payment transactions, capture pending payments, refund successful payments, retrieve payment details, and list payments using optional filters and pagination.

## Technologies

- Grails 5.3.6
- Groovy
- GORM / Hibernate
- H2 Database
- Gradle
- Spock Testing Framework

## Requirements

- Java 11 or later
- No separate Gradle installation is required because the project includes the Gradle Wrapper.

## Running the Application

### Windows PowerShell

```powershell
.\gradlew bootRun
```

### Linux or macOS

```bash
./gradlew bootRun
```

The application will run at:

```text
http://localhost:8080
```

## Running the Tests

### Windows PowerShell

```powershell
.\gradlew test
```

### Linux or macOS

```bash
./gradlew test
```

## Payment Statuses

A payment can have one of the following statuses:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `REFUNDED`

Supported status transitions:

```text
PENDING -> SUCCESS -> REFUNDED
```

A newly created payment always starts with the `PENDING` status.

## API Endpoints

### 1. Create Merchant

```http
POST /api/merchants
```

Request:

```bash
curl -X POST http://localhost:8080/api/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Store",
    "email": "store@test.com"
  }'
```

Example response:

```json
{
  "id": 1,
  "name": "Test Store",
  "email": "store@test.com",
  "apiKey": "generated-api-key",
  "active": true,
  "dateCreated": "2026-09-04T01:00:00Z"
}
```

The returned `apiKey` must be supplied in the `X-API-KEY` header when accessing payment endpoints.

---

### 2. Create Payment

```http
POST /api/payments
```

Request:

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: generated-api-key" \
  -d '{
    "reference": "INV-10001",
    "amount": 120.50,
    "currency": "USD",
    "description": "Order payment"
  }'
```

Example response:

```json
{
  "id": 1,
  "reference": "INV-10001",
  "amount": 120.50,
  "currency": "USD",
  "description": "Order payment",
  "status": "PENDING",
  "merchantId": 1,
  "dateCreated": "2026-09-04T01:00:00Z"
}
```

---

### 3. Capture Payment

Only payments with the `PENDING` status can be captured.

```http
POST /api/payments/{reference}/capture
```

Request:

```bash
curl -X POST http://localhost:8080/api/payments/INV-10001/capture \
  -H "X-API-KEY: generated-api-key"
```

Example response:

```json
{
  "id": 1,
  "reference": "INV-10001",
  "status": "SUCCESS"
}
```

---

### 4. Refund Payment

Only payments with the `SUCCESS` status can be refunded.

```http
POST /api/payments/{reference}/refund
```

Request:

```bash
curl -X POST http://localhost:8080/api/payments/INV-10001/refund \
  -H "X-API-KEY: generated-api-key"
```

Example response:

```json
{
  "id": 1,
  "reference": "INV-10001",
  "status": "REFUNDED"
}
```

---

### 5. Get Payment Details

```http
GET /api/payments/{reference}
```

Request:

```bash
curl http://localhost:8080/api/payments/INV-10001 \
  -H "X-API-KEY: generated-api-key"
```

---

### 6. List Merchant Payments

```http
GET /api/payments
```

Request:

```bash
curl http://localhost:8080/api/payments \
  -H "X-API-KEY: generated-api-key"
```

## Filters

The list endpoint supports the following optional filters:

- `status`
- `fromDate`
- `toDate`

Date values must use the following format:

```text
yyyy-MM-dd
```

Example:

```bash
curl "http://localhost:8080/api/payments?status=SUCCESS&fromDate=2026-09-01&toDate=2026-09-30" \
  -H "X-API-KEY: generated-api-key"
```

## Pagination

The list endpoint supports pagination using:

- `max`: maximum number of results. Default: `10`, maximum: `100`.
- `offset`: number of results to skip. Default: `0`.

Example:

```bash
curl "http://localhost:8080/api/payments?max=10&offset=0" \
  -H "X-API-KEY: generated-api-key"
```

Payments are returned from newest to oldest.

## Error Responses

Errors are returned as JSON:

```json
{
  "errorCode": "10",
  "error": "Only SUCCESS payments can be refunded"
}
```

Examples of handled errors include:

- Invalid API key
- Merchant validation errors
- Duplicate merchant email
- Duplicate payment reference
- Invalid payment amount
- Payment not found
- Invalid payment status transition
- Invalid status filter
- Invalid date format

## Business Rules

- Merchant email addresses must be unique.
- Merchant API keys are generated automatically.
- Only active merchants can access payment endpoints.
- Payment references must be unique.
- Payment amounts must be greater than zero.
- New payments start with the `PENDING` status.
- Only `PENDING` payments can be captured.
- Only `SUCCESS` payments can be refunded.
- A merchant can only access its own payments.
- Capture and refund operations run inside transactions.

## Notes and Assumptions

- The development environment uses an in-memory H2 database.
- Development data is removed when the application restarts.
- The `X-API-KEY` header is used as basic merchant authentication.
- Currency is supplied as a string such as `USD`.
- The `FAILED` status is included in the domain model but no failure-processing endpoint was required by the task.
## Running with Docker

Build the Docker image:

```bash
docker build -t payment-processing-service .
```

Run the container:

```bash
docker run -p 8080:8080 payment-processing-service
```

The application will be available at:

```text
http://localhost:8080
```