package ca.portfolio.payments.transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TransferRepository extends JpaRepository<TransferRecord, UUID> {
    Optional<TransferRecord> findByIdempotencyKey(String idempotencyKey);
    List<TransferRecord> findByFromWalletIdInOrToWalletIdInOrderByCreatedAtDesc(Collection<UUID> fromIds, Collection<UUID> toIds);
}
