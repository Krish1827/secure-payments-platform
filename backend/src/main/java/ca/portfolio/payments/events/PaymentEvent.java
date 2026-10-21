package ca.portfolio.payments.events;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record PaymentEvent(UUID transferId, UUID fromWalletId, UUID toWalletId, BigDecimal amount, String currency, Instant occurredAt) {}
