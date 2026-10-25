package tradance.repository;

import tradance.dto.OrderBookLevel;
import tradance.models.Order;
import tradance.models.OrderSide;
import tradance.models.OrderStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository {
    List<Order> findAllByUser(Long userId);

    List<OrderBookLevel> findOrderBook(Long currencyId, OrderSide side, int limit);

    Long save(Connection con, Order order);

    List<Order> findMatching(Connection con, Long currencyId, OrderSide side, BigDecimal price, Long excludeUserId);

    List<Order> findAll();

    void updateFill(Connection con, Long orderId, BigDecimal filled, OrderStatus status);

    Optional<Order> findById(Connection con, Long id);
}
