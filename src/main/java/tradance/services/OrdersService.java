package tradance.services;

import tradance.dto.OrderBookDto;
import tradance.dto.OrderForm;
import tradance.models.Order;
import tradance.models.Trade;

import java.util.List;

public interface OrdersService {
    Order place(Long userId, OrderForm form);
    void cancel(Long userId, Long orderId);
    OrderBookDto getOrderBook(String ticker);
    List<Order> findMyOrders(Long userId);
    List<Trade> findMyTrades(Long userId);
}
