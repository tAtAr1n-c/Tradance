package tradance.repository;


import tradance.models.Wallet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface WalletsRepository {
    Optional<Wallet> findById(Long id);

    List<Wallet> findAllByUser(Long userId);

    Optional<Wallet> findByUserAndCurrency(Long userId, Long currencyId);

    boolean lockFunds(Connection con, Long walletId, BigDecimal amount);

    void unlock(Connection con, Long walletId, BigDecimal amount);

    void spendLocked(Connection con, Long walletId, BigDecimal spend, BigDecimal unlock);

    void credit(Connection con, Long walletId, BigDecimal amount);

    Wallet findOrCreate(Connection con, Long userId, Long currencyId);


}
