package tradance.services;

import tradance.models.Order;
import tradance.models.OrderSide;
import tradance.models.Wallet;
import tradance.repository.CurrenciesRepository;
import tradance.repository.OrdersRepository;
import tradance.repository.TradesRepository;
import tradance.repository.WalletsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.List;

import static java.lang.Math.*;


public class MatchingEngineImpl implements MatchingEngine {

    private final OrdersRepository ordersRepository;
    private final WalletsRepository walletsRepository;
    private final TradesRepository tradesRepository;
    private final CurrenciesRepository currenciesRepository;
    private final BigDecimal feeRate;
    private final Long feeAccountId;

    public MatchingEngineImpl(OrdersRepository ordersRepository, WalletsRepository walletsRepository, TradesRepository tradesRepository, CurrenciesRepository currenciesRepository  , BigDecimal feeRate, Long feeAccountId) {
        this.ordersRepository = ordersRepository;
        this.walletsRepository = walletsRepository;
        this.tradesRepository = tradesRepository;
        this.currenciesRepository = currenciesRepository;
        this.feeRate = feeRate;
        this.feeAccountId = feeAccountId;
    }

    @Override
    public void match(Connection con, Order newOrder) {
        List<Order> counter = ordersRepository.findMatching(con, newOrder.getCurrencyId(), newOrder.getSide(), newOrder.getPrice(), newOrder.getUserId());

        Long quote = currenciesRepository.findQuote().getId();
        for(Order other : counter){
            if(newOrder.getRemaining().compareTo(BigDecimal.ZERO) < 0){
                break;
            }
            BigDecimal qty = newOrder.getRemaining().min(other.getRemaining());
            if(qty.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal tradePrice = other.getPrice();
            Order buyer = newOrder.getSide().equals(OrderSide.BUY) ? newOrder : other;
            Order seller = newOrder.getSide().equals(OrderSide.SELL) ? newOrder : other;
            BigDecimal total = tradePrice.multiply(qty).setScale(8, RoundingMode.HALF_UP);
            BigDecimal fee = total.multiply(feeRate).setScale(8, RoundingMode.DOWN);



            Wallet buyerUSDT;
            Wallet buyerCoin;
            Wallet sellerCoin;
            Wallet sellerUSDT;
            Wallet feeUSDT;


        }
    }
}
