package tradance.repository;

import tradance.models.Trade;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public interface TradesRepository {
    List<Trade> findLastByCurrency(Long currencyId, int limit);

    BigDecimal findLastPrice(Long currencyId);

    List<Trade> findAllByUser(Long userId);

    void save(Connection con, Trade trade);

    List<Trade> findAll();

}
