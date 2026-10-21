package ca.portfolio.payments.wallet;

import ca.portfolio.payments.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private AppUser owner;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false, length = 3) private String currency;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected Wallet() {}
    public Wallet(AppUser owner, String currency) { this.owner = owner; this.currency = currency; }
    public UUID getId() { return id; }
    public AppUser getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public void debit(BigDecimal amount) { this.balance = this.balance.subtract(amount); }
    public void credit(BigDecimal amount) { this.balance = this.balance.add(amount); }
}
