package tradance.services;


import lombok.Synchronized;
import tradance.dto.OrderBookDto;
import tradance.dto.OrderBookLevel;
import tradance.dto.OrderForm;
import tradance.exceptions.BusinessException;
import tradance.models.*;
import tradance.models.Currency;
import tradance.repository.CurrenciesRepository;
import tradance.repository.OrdersRepository;
import tradance.repository.TradesRepository;
import tradance.repository.WalletsRepository;
import tradance.exceptions.ValidationException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

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
        if (form.getSide() == null || !(form.getSide().equals("BUY") || form.getSide().equals("SELL"))) {
            throw new BusinessException("Такой стороны сделки нет");
        }
        if(form.getCurrency() == null){
            throw new BusinessException("Такой монеты нет");
        }
        Optional<Currency> currency = currenciesRepository.findByTicker(form.getCurrency().trim().toUpperCase());
        if(currency.isEmpty() || !currency.get().isActive() || currency.get().isQuote()){
            errors.put("currency", "Монета недоступна");
        }
        BigDecimal price = null;
        try {
            price = new BigDecimal(form.getPrice().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) errors.put("price", "Цена должна быть больше нуля");
            else if (price.scale() > 8) errors.put("price", "Не больше 8 знаков после запятой");
        } catch (NumberFormatException | NullPointerException e) {
            errors.put("price", "Введите число");
        }

        BigDecimal amount = null;
        try {
            amount = new BigDecimal(form.getAmount().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) errors.put("amount", "Количество должно быть больше нуля");
            else if (amount.scale() > 8) errors.put("amount", "Не больше 8 знаков после запятой");
        } catch (NumberFormatException | NullPointerException e) {
            errors.put("amount", "Введите количество");
        }
        if (!errors.isEmpty()) throw new ValidationException(errors);
        try(Connection con = dataSource.getConnection()){
            con.setAutoCommit(false);
            try{
                Long walletCurrencyId = (OrderSide.valueOf(form.getSide()).equals(OrderSide.BUY)) ? currenciesRepository.findQuote().getId() : currency.get().getId();
                Wallet wallet = walletsRepository.findOrCreate(con, userId, walletCurrencyId);
                BigDecimal need = null;
                if(OrderSide.valueOf(form.getSide()).equals(OrderSide.BUY)){
                    need = price.multiply(amount).setScale(8, RoundingMode.UP);
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
                        .price(price)
                        .amount(amount)
                        .filled(BigDecimal.ZERO)
                        .status(OrderStatus.OPEN)
                        .build();

                ordersRepository.save(con, order);
                matchingEngine.match(con, order);
                con.commit();
                return order;
            }catch(SQLException | RuntimeException e){
                con.rollback();
                throw e;
            }
            finally{
                con.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }

    @Synchronized
    @Override
    public void cancel(Long userId, Long orderId) {
        if(orderId == null) throw new BusinessException("Заявка не найдена");
        try(Connection con = dataSource.getConnection()){
            con.setAutoCommit(false);

            try{
                Order order = ordersRepository.findById(con, orderId).orElseThrow(() -> new BusinessException("Заявка не найдена"));
                if(!order.getUserId().equals(userId)) throw new BusinessException("Это не ваша заявка");
                if(order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.PARTIAL) throw new BusinessException("Заявка уже закрыта");
                Wallet wallet = walletsRepository.findByUserAndCurrency(userId, order.getCurrencyId()).orElseThrow(() -> new BusinessException("Кошелек не найден"));

                if(order.getSide() == OrderSide.BUY){
                    Wallet usdt = walletsRepository.findOrCreate(con, userId, currenciesRepository.findQuote().getId());
                    walletsRepository.unlock(con, usdt.getId(), order.getPrice().multiply(order.getRemaining()));
                }else{
                    Wallet coin = walletsRepository.findOrCreate(con, userId, order.getCurrencyId());
                    walletsRepository.unlock(con, coin.getId(), order.getRemaining());
                }

                ordersRepository.updateFill(con, order.getId(), order.getFilled(), OrderStatus.CANCELLED);
                con.commit();
            }catch(SQLException | RuntimeException e){
                con.rollback();
                throw e;
            }finally{
                con.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }

    @Override
    public OrderBookDto getOrderBook(String ticker) {
        if(ticker == null || ticker.isBlank()) throw new BusinessException("Тикер пустой");
        Currency currency = currenciesRepository.findByTicker(ticker.trim().toUpperCase()).orElseThrow(() -> new BusinessException("Монета не найдена"));
        if(!currency.isActive() || currency.isQuote()) throw new BusinessException("Монета не найдена");
        List<OrderBookLevel> asks = ordersRepository.findOrderBook(currency.getId(), OrderSide.SELL, 15);
        Collections.reverse(asks);
        return OrderBookDto.builder()
                .bids(ordersRepository.findOrderBook(currency.getId(), OrderSide.BUY, 15))
                .asks(asks)
                .lastPrice(tradesRepository.findLastPrice(currency.getId()))
                .lastTrades(tradesRepository.findLastByCurrency(currency.getId(), 20))
                .build();
    }

    @Override
    public List<Order> findMyOrders(Long userId) {
        if(userId == null) return new ArrayList<Order>();
        return ordersRepository.findAllByUser(userId);
    }
}
