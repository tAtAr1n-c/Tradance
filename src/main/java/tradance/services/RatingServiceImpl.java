package tradance.services;

import tradance.dto.RatingRow;
import tradance.models.*;

import tradance.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;


public class RatingServiceImpl implements RatingService{

    private final UsersRepository usersRepository;
    private final WalletsRepository walletsRepository;
    private final CurrenciesRepository currenciesRepository;
    private final TradesRepository tradesRepository;
    private final DepositsRepository depositsRepository;

    public RatingServiceImpl(UsersRepository usersRepository, WalletsRepository walletsRepository, CurrenciesRepository currenciesRepository, TradesRepository tradesRepository, DepositsRepository depositsRepository) {
        this.usersRepository = usersRepository;
        this.walletsRepository = walletsRepository;
        this.currenciesRepository = currenciesRepository;
        this.tradesRepository = tradesRepository;
        this.depositsRepository = depositsRepository;
    }

    @Override
    public List<RatingRow> getRating() {
        List<RatingRow> ratingRows = new ArrayList<>();
        Map<Long, BigDecimal> prices = new HashMap<>();
        for(Currency currency : currenciesRepository.findAll()){
            BigDecimal lastPrice = tradesRepository.findLastPrice(currency.getId());
            if(!currency.isQuote()){
                prices.put(currency.getId(), (lastPrice == null) ? BigDecimal.ZERO : lastPrice);
            }
            else{
                prices.put(currency.getId(), BigDecimal.ONE);
            }
        }
        List<Deposit> deposits = depositsRepository.findAll();
        for(User user : usersRepository.findAll()){
            if(user.getRole() == Role.ADMIN){
                continue;
            }
            BigDecimal portfolio = BigDecimal.ZERO;
            for(Wallet wallet : walletsRepository.findAllByUser(user.getId())){
                portfolio = portfolio.add(wallet.getBalance().multiply(prices.get(wallet.getCurrencyId())));
            }

            BigDecimal invested = new BigDecimal("10000");
            for(Deposit deposit : deposits){
                if(deposit.getUserId().equals(user.getId())){
                    invested = invested.add(deposit.getAmount().multiply(prices.get(deposit.getCurrencyId())));
                }
            }
            BigDecimal profit = portfolio.subtract(invested);
            BigDecimal profitPct = profit.multiply(new BigDecimal("100")).divide(invested, 2, RoundingMode.HALF_UP);
            int tradesCount = tradesRepository.findAllByUser(user.getId()).size();
            if(tradesCount == 0) continue;

            RatingRow ratingRow = RatingRow.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .avatarFileId(user.getAvatarFileId())
                    .portfolio(portfolio)
                    .invested(invested)
                    .profit(profit)
                    .profitPct(profitPct)
                    .tradesCount(tradesCount)
                    .build();

            ratingRows.add(ratingRow);
        }

        ratingRows.sort(Comparator.comparing(RatingRow::getProfit).reversed());
        return ratingRows;
    }
}
