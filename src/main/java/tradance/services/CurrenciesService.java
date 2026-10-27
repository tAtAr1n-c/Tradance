package tradance.services;

import tradance.dto.MarketRow;

import java.util.List;

public interface CurrenciesService {
    List<MarketRow> getMarket(Long userIdOrNull);
}
