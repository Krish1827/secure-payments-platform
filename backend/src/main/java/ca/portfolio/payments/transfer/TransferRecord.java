package ca.portfolio.payments.transfer;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers", uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"))
public class TransferRecord {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID fromWalletId;
    @Column(nullable = false) private UUID toWalletId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(length = 240) private String description;
    @Column(nullable = false, unique = true, length = 120) private String idempotencyKey;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected TransferRecord() {}
    public TransferRecord(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String currency, String description, String idempotencyKey) {
        this.fromWalletId = fromWalletId; this.toWalletId = toWalletId; this.amount = amount;
        this.currency = currency; this.description = description; this.idempotencyKey = idempotencyKey;
    }
    public UUID getId() { return id; }
    public UUID getFromWalletId() { return fromWalletId; }
    public UUID getToWalletId() { return toWalletId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
}
