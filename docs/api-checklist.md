# API checklist

- [ ] `POST /api/auth/register` — create user and receive token
- [ ] `POST /api/auth/login` — authenticate and receive token
- [ ] `POST /api/wallets?currency=CAD` — create wallet
- [ ] `GET /api/wallets` — list wallets belonging to current user
- [ ] `POST /api/transfers` — send transfer with `Idempotency-Key`
- [ ] `GET /api/transfers` — list transfers involving current user's wallets
- [ ] `GET /actuator/health` — health status
- [ ] Swagger UI — `/swagger-ui/index.html`

## Test scenarios to add
- Registration rejects duplicate email.
- Login rejects wrong password.
- Unauthenticated wallet request is rejected.
- User cannot transfer from another user's wallet.
- Zero/negative amount is rejected.
- Insufficient funds does not change balances.
- Currency mismatch is rejected.
- Same-wallet transfer is rejected.
- Repeated idempotency key does not execute the transfer twice.
- Concurrent transfers cannot overspend a wallet.
