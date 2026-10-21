package ca.portfolio.payments.transfer;

import ca.portfolio.payments.wallet.WalletRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TransferController {
    private final TransferService service;
    private final WalletRepository wallets;
    private final TransferRepository transfers;
    public TransferController(TransferService service, WalletRepository wallets, TransferRepository transfers) {
        this.service = service; this.wallets = wallets; this.transfers = transfers;
    }

    @PostMapping("/transfers")
    public TransferDtos.TransferResponse transfer(Authentication auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody TransferDtos.TransferRequest request) {
        return service.transfer(auth.getName(), key, request);
    }

    @GetMapping("/transfers")
    public List<TransferDtos.TransferResponse> history(Authentication auth) {
        var ids = wallets.findOwnedByEmail(auth.getName()).stream().map(w -> w.getId()).toList();
        return transfers.findByFromWalletIdInOrToWalletIdInOrderByCreatedAtDesc(ids, ids)
                .stream().map(t -> new TransferDtos.TransferResponse(t.getId(), t.getFromWalletId(),
                        t.getToWalletId(), t.getAmount(), t.getCurrency(), t.getDescription(),
                        "COMPLETED", t.getCreatedAt())).toList();
    }
}
