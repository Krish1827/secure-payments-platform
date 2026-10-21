# Secure Digital Payments Platform (Portfolio Project)

A learning/demo digital-wallet platform built with Spring Boot, PostgreSQL, React, Docker Compose, and Kafka. It simulates wallet-to-wallet transfers; it does **not** connect to real banks or process real money.

## Features
- Java 21 + Spring Boot REST API
- PostgreSQL persistence with Spring Data JPA / Hibernate
- JWT-based authentication and role-based authorization
- Wallet creation and balance lookup
- Wallet-to-wallet transfers with validation and idempotency keys
- Explicit demo-only wallet funding endpoint for testing fake balances
- Transfer history and audit-friendly transaction records
- Kafka payment event publication (best-effort event notification)
- React/Vite frontend starter
- Docker Compose for PostgreSQL, Kafka (KRaft), backend, and frontend
- Unit/integration test starter and GitHub Actions CI

## Important design note
This is an educational starter, not production-ready financial software. It uses a simple database transaction for transfers and publishes a Kafka event after the transaction method returns. For production-grade guarantees, use a transactional outbox, robust ledger accounting, reconciliation, rate limits, key rotation, secrets management, observability, and security review. Never use real payment credentials or real funds.

## Prerequisites
- Docker Desktop
- Or Java 21, Maven 3.9+, Node.js 20+

## Run with Docker
```bash
docker compose up --build
```
- Frontend: http://localhost:5173
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

Demo user is **not** pre-created. Register through the API, then create a wallet.

## Run locally
1. Start PostgreSQL and Kafka using `docker compose up postgres kafka -d`.
2. Backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. Frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## API quick start
Register:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"ChangeMe123!"}'
```
Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"ChangeMe123!"}'
```
Use the returned `token` as `Authorization: Bearer <token>`.

Create wallet:
```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer YOUR_TOKEN"
```
Add fake demo funds (only for this portfolio demo):
```bash
curl -X POST "http://localhost:8080/api/wallets/YOUR_WALLET_UUID/demo-credit?amount=100.00" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
List your wallets:
```bash
curl http://localhost:8080/api/wallets \
  -H "Authorization: Bearer YOUR_TOKEN"
```
Transfer:
```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-transfer-001" \
  -d '{"fromWalletId":"SOURCE_WALLET_UUID","toWalletId":"DESTINATION_WALLET_UUID","amount":10.00,"currency":"CAD","description":"Demo transfer"}'
```
Transfers can only be initiated from a wallet owned by the authenticated user. The destination wallet must exist and use the same currency.

## Security checklist before showcasing
- Change all development secrets and credentials.
- Do not expose PostgreSQL/Kafka ports publicly.
- Use HTTPS, secure cookie/token handling, rate limiting, monitoring, and secrets manager for production.
- Add refresh-token rotation, email verification, MFA, account lockout, and fraud controls as future work.
- Review concurrency and idempotency behavior before treating this as production code.

## Suggested portfolio evidence
1. Architecture diagram and sequence diagram in `docs/`.
2. Screenshots of registration, wallet list, and transfer history.
3. Tests for insufficient funds, invalid amount, unauthorized access, and duplicate idempotency key.
4. A short demo video using only fake accounts and fake money.
