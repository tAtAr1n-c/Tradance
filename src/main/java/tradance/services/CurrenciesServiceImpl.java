package tradance.services;

import tradance.dto.MarketRow;
import tradance.models.Currency;
import tradance.models.Trade;
import tradance.repository.CurrenciesRepository;
import tradance.repository.TradesRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CurrenciesServiceImpl implements CurrenciesService {

    private final CurrenciesRepository currenciesRepository;
    private final TradesRepository tradesRepository;
    private final FileStorageService fileStorageService;

    public CurrenciesServiceImpl(CurrenciesRepository currenciesRepository, TradesRepository tradesRepository, FileStorageService fileStorageService) {
        this.currenciesRepository = currenciesRepository;
        this.tradesRepository = tradesRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<MarketRow> getMarket(Long userIdOrNull) {

        Set<Long> favorites = (userIdOrNull == null) ? new HashSet<>() : currenciesRepository.findFavoriteIds(userIdOrNull);
        List<MarketRow> marketRows = new ArrayList<>();
        List<Currency> all = currenciesRepository.findAllActiveTradable();


        for(Currency currency : all){
            List<Trade> lastTwoPrice = tradesRepository.findLastByCurrency(currency.getId(), 2);
            String trend;
            BigDecimal priceLast = null;
            BigDecimal pricePreLast;
            if(lastTwoPrice.isEmpty()){
                trend = "NONE";
            }else if(lastTwoPrice.size() == 1){
                priceLast = lastTwoPrice.getFirst().getPrice();
                trend = "FLAT";
            }else {
                priceLast = lastTwoPrice.getFirst().getPrice();
                pricePreLast = lastTwoPrice.getLast().getPrice();

                if(priceLast.compareTo(pricePreLast) > 0) {
                    trend = "UP";
                }else if(priceLast.compareTo(pricePreLast) < 0){
                    trend = "DOWN";
                }else{
                    trend = "FLAT";
                }
            }



            marketRows.add(MarketRow.builder()
                    .currencyId(currency.getId())
                    .ticker(currency.getTicker())
                    .name(currency.getName())
                    .iconPath(currency.getIconPath())
                    .lastPrice(priceLast)
                    .trend(trend)
                    .favorite(favorites.contains(currency.getId()))
                    .build());
        }

        return marketRows;

    }
}
