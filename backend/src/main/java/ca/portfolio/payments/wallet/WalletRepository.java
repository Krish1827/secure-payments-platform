package ca.portfolio.payments.wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Query("select w from Wallet w join fetch w.owner where w.owner.email = :email order by w.createdAt desc")
    List<Wallet> findOwnedByEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w join fetch w.owner where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);
}
