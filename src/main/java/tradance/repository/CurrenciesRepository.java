package tradance.repository;

import tradance.models.Currency;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CurrenciesRepository extends CrudRepository<Currency> {
    Optional<Currency> findByTicker(String ticker);

    List<Currency> findAllActiveTradable();

    Currency findQuote();

    boolean isUsed(Long currencyId);

    Set<Long> findFavoriteIds(Long userId);

    void addFavorite(Long userId, Long currencyId);

    void removeFavorite(Long userId, Long currencyId);

    boolean isFavorite(Long userId, Long currencyId);
}
