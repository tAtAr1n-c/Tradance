package tradance.services;

import tradance.dto.CurrencyForm;
import tradance.dto.MarketRow;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.models.Currency;
import tradance.models.Trade;
import tradance.repository.CurrenciesRepository;
import tradance.repository.TradesRepository;

import java.math.BigDecimal;
import java.util.*;

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

    @Override
    public List<Currency> findAll() {
        return currenciesRepository.findAll();
    }

    @Override
    public Currency findById(Long id) {
        if(id == null) throw new BusinessException("Id монеты не может быть пустым" );
        return currenciesRepository.findById(id).orElseThrow(() -> new BusinessException("монета не найдена"));
    }

    @Override
    public void create(CurrencyForm form) {

        validate(form, null);


        Currency currency = Currency.builder()
                .ticker(form.getTicker().trim())
                .name(form.getName())
                .iconPath(null)
                .quote(false)
                .active("on".equals(form.getActive()))
                .build();
        currenciesRepository.save(currency);
    }

    @Override
    public void update(Long id, CurrencyForm form) {


        Currency curPrev = currenciesRepository.findById(id).orElseThrow(() -> new BusinessException("Такой монеты нет под айди "  + id));
        validate(form, id);

        Currency currency = Currency.builder()
                .id(id)
                .ticker(form.getTicker().trim())
                .name(form.getName())
                .iconPath(curPrev.getIconPath())
                .quote(curPrev.isQuote())
                .active("on".equals(form.getActive()))
                .build();
        currenciesRepository.update(currency);

    }

    @Override
    public void delete(Long id) {
        if(id == null) throw new BusinessException("id не может быть пустым");
        if(currenciesRepository.isUsed(id)) throw new BusinessException("Монета используется, её можно только выключить");
        if(currenciesRepository.findQuote().getId().equals(id)) throw new BusinessException("Эта монета quote её нельзя удалить");
        currenciesRepository.delete(id);
    }

    private void validate(CurrencyForm currencyForm, Long idOrNull){
        Map<String, String> errors = new TreeMap<>();

        String ticker = currencyForm.getTicker();
        if(ticker == null || !ticker.matches("[A-Z]{2,10}")) errors.put("ticker", "Тикер: 2–10 заглавных латинских букв");

        if (!errors.containsKey("ticker")) {
            Optional<Currency> existing = currenciesRepository.findByTicker(ticker);
            if (existing.isPresent() && !existing.get().getId().equals(idOrNull)) {
                errors.put("ticker", "Тикер уже занят");
            }
        }
        String name = currencyForm.getName();
        if(name == null || name.isBlank()){
            errors.put("name", "Введите название");

        }else if(name.trim().length() > 50){
            errors.put("name", "Слишком длинное название");
        }

        if(!errors.isEmpty()) throw new ValidationException(errors);

    }

    @Override
    public void setActive(Long id, boolean active) {
        if(id == null) throw new BusinessException("id не может быть null");
        Currency currency = currenciesRepository.findById(id).orElseThrow(() -> new BusinessException("Не удалось найти монету и изменить ее"));
        if(currency.isQuote()) throw new BusinessException("Котировочную валюту выключать нельзя");
        currency.setActive(active);
        currenciesRepository.update(currency);
    }
}
