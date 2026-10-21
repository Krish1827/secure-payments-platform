package ca.portfolio.payments.wallet;

import ca.portfolio.payments.user.UserRepository;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletRepository wallets;
    private final UserRepository users;
    public WalletController(WalletRepository wallets, UserRepository users) { this.wallets = wallets; this.users = users; }

    public record WalletResponse(UUID id, BigDecimal balance, String currency, Instant createdAt) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse create(Authentication auth, @RequestParam(defaultValue = "CAD") @Pattern(regexp = "CAD|USD") String currency) {
        var user = users.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Wallet w = wallets.save(new Wallet(user, currency));
        return response(w);
    }

    @PostMapping("/{id}/demo-credit")
    public WalletResponse demoCredit(Authentication auth, @PathVariable UUID id,
                                     @RequestParam BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive with at most 2 decimal places");
        Wallet wallet = wallets.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        if (!wallet.getOwner().getEmail().equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this wallet");
        wallet.credit(amount.setScale(2, RoundingMode.UNNECESSARY));
        return response(wallets.save(wallet));
    }

    @GetMapping
    public List<WalletResponse> list(Authentication auth) {
        return wallets.findOwnedByEmail(auth.getName()).stream().map(WalletController::response).toList();
    }

    private static WalletResponse response(Wallet w) {
        return new WalletResponse(w.getId(), w.getBalance(), w.getCurrency(), w.getCreatedAt());
    }
}
