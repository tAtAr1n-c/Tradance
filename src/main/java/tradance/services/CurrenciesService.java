package tradance.services;

import tradance.dto.CurrencyForm;
import tradance.dto.MarketRow;
import tradance.models.Currency;

import java.util.List;

public interface CurrenciesService {
    List<MarketRow> getMarket(Long userIdOrNull);
    List<Currency> findAll();
    Currency findById(Long id);
    void create(CurrencyForm form);
    void update(Long id, CurrencyForm form);
    void delete(Long id);
    void setActive(Long id, boolean active);
}
