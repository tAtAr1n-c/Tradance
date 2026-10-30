package tradance.services;

import tradance.models.*;
import tradance.repository.CurrenciesRepository;
import tradance.repository.OrdersRepository;
import tradance.repository.TradesRepository;
import tradance.repository.WalletsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.List;


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

        Long quoteId = currenciesRepository.findQuote().getId();
        for(Order other : counter){
            if(newOrder.getRemaining().compareTo(BigDecimal.ZERO) <= 0){
                break;
            }
            BigDecimal qty = newOrder.getRemaining().min(other.getRemaining());
            if(qty.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal tradePrice = other.getPrice();
            Order buyer = newOrder.getSide().equals(OrderSide.BUY) ? newOrder : other;
            Order seller = newOrder.getSide().equals(OrderSide.SELL) ? newOrder : other;
            BigDecimal total = tradePrice.multiply(qty).setScale(8, RoundingMode.HALF_UP);
            BigDecimal fee = total.multiply(feeRate).setScale(8, RoundingMode.DOWN);



            Wallet buyerUSDT = walletsRepository.findOrCreate(con, buyer.getUserId(), quoteId);
            Wallet buyerCoin = walletsRepository.findOrCreate(con, buyer.getUserId(), buyer.getCurrencyId());
            Wallet sellerCoin = walletsRepository.findOrCreate(con, seller.getUserId(), seller.getCurrencyId());
            Wallet sellerUSDT = walletsRepository.findOrCreate(con, seller.getUserId(), quoteId);
            Wallet feeUSDT  = walletsRepository.findOrCreate(con, feeAccountId, quoteId);

            walletsRepository.spendLocked(con, buyerUSDT.getId(), total, buyer.getPrice().multiply(qty));
            walletsRepository.credit(con, buyerCoin.getId(), qty);

            walletsRepository.spendLocked(con, sellerCoin.getId(), qty, qty);
            walletsRepository.credit(con, sellerUSDT.getId(), total.subtract(fee));

            walletsRepository.credit(con, feeUSDT.getId(), fee);

            Trade trade = Trade.builder()
                    .buyOrderId(buyer.getId())
                    .sellOrderId(seller.getId())
                    .price(tradePrice)
                    .amount(qty)
                    .build();
            tradesRepository.save(con, trade);

            applyFill(con, other, qty);
            applyFill(con, newOrder, qty);

        }

    }


    private void applyFill(Connection con, Order order, BigDecimal qty) {
        BigDecimal filled = order.getFilled().add(qty);
        OrderStatus status = filled.compareTo(order.getAmount()) == 0 ? OrderStatus.FILLED : OrderStatus.PARTIAL;

        ordersRepository.updateFill(con, order.getId(), filled, status);

        order.setFilled(filled);
        order.setStatus(status);
    }
}
