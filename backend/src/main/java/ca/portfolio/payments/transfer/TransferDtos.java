package ca.portfolio.payments.transfer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public final class TransferDtos {
    private TransferDtos() {}
    public record TransferRequest(@NotNull UUID fromWalletId, @NotNull UUID toWalletId,
                                  @NotNull @DecimalMin(value = "0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount,
                                  @Pattern(regexp = "CAD|USD") String currency,
                                  @Size(max = 240) String description) {}
    public record TransferResponse(UUID id, UUID fromWalletId, UUID toWalletId,
                                   BigDecimal amount, String currency, String description,
                                   String status, java.time.Instant createdAt) {}
}
