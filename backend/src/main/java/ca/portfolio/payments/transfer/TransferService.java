package ca.portfolio.payments.transfer;

import ca.portfolio.payments.events.PaymentEvent;
import ca.portfolio.payments.events.PaymentEventPublisher;
import ca.portfolio.payments.wallet.Wallet;
import ca.portfolio.payments.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferService {
    private final WalletRepository wallets;
    private final TransferRepository transfers;
    private final PaymentEventPublisher events;
    public TransferService(WalletRepository wallets, TransferRepository transfers, PaymentEventPublisher events) {
        this.wallets = wallets; this.transfers = transfers; this.events = events;
    }

    @Transactional
    public TransferDtos.TransferResponse transfer(String email, String key, TransferDtos.TransferRequest request) {
        if (key == null || key.isBlank() || key.length() > 120)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid Idempotency-Key header is required");
        var existing = transfers.findByIdempotencyKey(key);
        if (existing.isPresent()) return response(existing.get());

        if (request.fromWalletId().equals(request.toWalletId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination wallets must differ");
        if (request.amount().scale() > 2 || request.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive and have at most 2 decimal places");

        // Lock in a stable order to reduce deadlock risk.
        UUID firstId = request.fromWalletId().compareTo(request.toWalletId()) < 0 ? request.fromWalletId() : request.toWalletId();
        UUID secondId = firstId.equals(request.fromWalletId()) ? request.toWalletId() : request.fromWalletId();
        Wallet first = wallets.findByIdForUpdate(firstId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        Wallet second = wallets.findByIdForUpdate(secondId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        Wallet source = first.getId().equals(request.fromWalletId()) ? first : second;
        Wallet destination = first.getId().equals(request.toWalletId()) ? first : second;

        if (!source.getOwner().getEmail().equals(email))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own the source wallet");
        if (!source.getCurrency().equals(destination.getCurrency()) || !source.getCurrency().equals(request.currency()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency mismatch");
        if (source.getBalance().compareTo(request.amount()) < 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");

        source.debit(request.amount());
        destination.credit(request.amount());
        TransferRecord record = transfers.save(new TransferRecord(source.getId(), destination.getId(),
                request.amount(), source.getCurrency(), request.description(), key));
        // Note: for durable event delivery, replace this with a transactional outbox.
        events.publish(new PaymentEvent(record.getId(), source.getId(), destination.getId(),
                request.amount(), source.getCurrency(), Instant.now()));
        return response(record);
    }

    private static TransferDtos.TransferResponse response(TransferRecord t) {
        return new TransferDtos.TransferResponse(t.getId(), t.getFromWalletId(), t.getToWalletId(),
                t.getAmount(), t.getCurrency(), t.getDescription(), "COMPLETED", t.getCreatedAt());
    }
}
