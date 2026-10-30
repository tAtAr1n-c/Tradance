package tradance.services;


import lombok.Synchronized;
import tradance.dto.OrderBookDto;
import tradance.dto.OrderForm;
import tradance.exceptions.BusinessException;
import tradance.models.*;
import tradance.repository.CurrenciesRepository;
import tradance.repository.OrdersRepository;
import tradance.repository.TradesRepository;
import tradance.repository.WalletsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrdersServiceImpl implements OrdersService {

    private final DataSource dataSource;
    private final OrdersRepository ordersRepository;
    private final CurrenciesRepository currenciesRepository;
    private final TradesRepository tradesRepository;
    private final WalletsRepository walletsRepository;
    private final MatchingEngine matchingEngine;

    public OrdersServiceImpl(DataSource dataSource, OrdersRepository ordersRepository, CurrenciesRepository currenciesRepository, TradesRepository tradesRepository,WalletsRepository walletsRepository , MatchingEngine matchingEngine) {
        this.dataSource = dataSource;
        this.ordersRepository = ordersRepository;
        this.currenciesRepository = currenciesRepository;
        this.tradesRepository = tradesRepository;
        this.walletsRepository = walletsRepository;
        this.matchingEngine = matchingEngine;
    }

    @Synchronized
    @Override
    public Order place(Long userId, OrderForm form) {
        Map<String, String> errors = new HashMap<>();
        Optional<Currency> currency = currenciesRepository.findByTicker(form.getCurrency().trim().toUpperCase());
        if(currency.isEmpty() || !currency.get().isActive() || currency.get().isQuote()){
            errors.put("currency", "Монета недоступна");
        }
        if(!(form.getSide().equals("BUY") || form.getSide().equals("SELL"))){
            throw new BusinessException("Такой стороны сделки нет");
        }
        BigDecimal price = null;
        try {
            price = new BigDecimal(form.getPrice().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) errors.put("price", "Цена должна быть больше нуля");
        } catch (NumberFormatException | NullPointerException e) {
            errors.put("price", "Введите число");
        }

        BigDecimal amount = null;
        try {
            amount = new BigDecimal(form.getAmount().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) errors.put("price", "Количество должно быть больше нуля");
        } catch (NumberFormatException | NullPointerException e) {
            errors.put("amount", "Введите количество");
        }
        try(Connection con = dataSource.getConnection()){
            con.setAutoCommit(false);
            try{
                Long walletCurrencyId = (form.getSide().equals(OrderSide.BUY)) ? currenciesRepository.findQuote().getId() : currency.get().getId();
                Wallet wallet = walletsRepository.findOrCreate(con, userId, walletCurrencyId);
                BigDecimal need = null;
                if(form.getSide().equals(OrderSide.BUY)){
                    need = price.multiply(amount).setScale(8, RoundingMode.UP)
                }else{
                    need = amount;
                }
                if(!walletsRepository.lockFunds(con, wallet.getId(), need)){
                    throw new BusinessException("Недостаточно средств");
                }
                Order order = Order.builder()
                        .userId(userId)
                        .currencyId(currency.get().getId())
                        .side(OrderSide.valueOf(form.getSide()))
                        .price(new BigDecimal(form.getPrice()))
                        .amount(new BigDecimal(form.getAmount()))
                        .filled(BigDecimal.ZERO)
                        .status(OrderStatus.OPEN)
                        .build();

                ordersRepository.save(con, order);
                matchingEngine.match(con, order);
                con.commit();

            }catch(RuntimeException e){
                throw new IllegalStateException("Не удалось найти кошелек пользователя");
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }

    @Override
    public void cancel(Long userId, Long orderId) {

    }

    @Override
    public OrderBookDto getOrderBook(String ticker) {
        return null;
    }

    @Override
    public List<Order> findMyOrders(Long userId) {
        return List.of();
    }
}
