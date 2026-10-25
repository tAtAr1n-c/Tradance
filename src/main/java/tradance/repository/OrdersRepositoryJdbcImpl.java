package tradance.repository;

import tradance.dto.OrderBookLevel;
import tradance.models.Order;
import tradance.models.OrderSide;
import tradance.models.OrderStatus;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class OrdersRepositoryJdbcImpl implements OrdersRepository {

    private final DataSource dataSource;


    private static final String SQL_FIND_ALL_BY_USER = "select id, user_id, currency_id, side, price, amount, filled, status, created_at from orders where user_id=? order by created_at desc";
    private static final String SQL_FIND_ALL = "select id, user_id, currency_id, side, price, amount, filled, status, created_at from orders order by created_at desc";
    private static final String SQL_FIND_BY_ID = "select id, user_id, currency_id, side, price, amount, filled, status, created_at from orders where id=? for update";

    private static final String SQL_ORDER_BOOK_ASKS = "select price, sum(amount - filled) as amount from orders where currency_id=? and side='SELL' and status in ('OPEN', 'PARTIAL') group by price order by price limit ?";
    private static final String SQL_ORDER_BOOK_BIDS = "select price, sum(amount - filled) as amount from orders where currency_id=? and side='BUY' and status in ('OPEN', 'PARTIAL') group by price order by price desc limit ?";

    private static final String SQL_MATCHING_FOR_BUY = "select id, user_id, currency_id, side, price, amount, filled, status, created_at from orders where currency_id=? and side='SELL' and status in ('OPEN', 'PARTIAL') and price<=? and user_id<>? order by price, created_at for update";
    private static final String SQL_MATCHING_FOR_SELL = "select id, user_id, currency_id, side, price, amount, filled, status, created_at from orders where currency_id=? and side='BUY' and status in ('OPEN', 'PARTIAL') and price>=? and user_id<>? order by price desc, created_at for update";

    private static final String SQL_SAVE_ORDER = "insert into orders (user_id, currency_id, side, price, amount, filled, status) values (?, ?, ?, ?, ?, ?, ?) returning id";
    private static final String SQL_UPDATE_FILL = "update orders set filled=?, status=? where id=?";


    public OrdersRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public List<Order> findAllByUser(Long userId) {
        List<Order> orders = new ArrayList<>();
        if (userId == null) return orders;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL_BY_USER)) {
            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) orders.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти заявки пользователя", e);
        }
        return orders;
    }

    @Override
    public List<OrderBookLevel> findOrderBook(Long currencyId, OrderSide side, int limit) {
        List<OrderBookLevel> levels = new ArrayList<>();
        if (currencyId == null || side == null) return levels;

        String sql = side == OrderSide.SELL ? SQL_ORDER_BOOK_ASKS : SQL_ORDER_BOOK_BIDS;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, currencyId);
            preparedStatement.setInt(2, limit);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    levels.add(OrderBookLevel.builder()
                            .price(resultSet.getBigDecimal("price"))
                            .amount(resultSet.getBigDecimal("amount"))
                            .build());
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось загрузить стакан", e);
        }
        return levels;
    }

    @Override
    public Long save(Connection con, Order order) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_SAVE_ORDER)) {
            preparedStatement.setLong(1, order.getUserId());
            preparedStatement.setLong(2, order.getCurrencyId());
            preparedStatement.setString(3, order.getSide().name());
            preparedStatement.setBigDecimal(4, order.getPrice());
            preparedStatement.setBigDecimal(5, order.getAmount());
            preparedStatement.setBigDecimal(6, order.getFilled() == null ? BigDecimal.ZERO : order.getFilled());
            preparedStatement.setString(7, order.getStatus().name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    order.setId(resultSet.getLong("id"));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не получилось сохранить заявку", e);
        }
        return order.getId();
    }

    @Override
    public List<Order> findMatching(Connection con, Long currencyId, OrderSide side, BigDecimal price, Long excludeUserId) {
        List<Order> orders = new ArrayList<>();

        String sql = side == OrderSide.BUY ? SQL_MATCHING_FOR_BUY : SQL_MATCHING_FOR_SELL;

        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setLong(1, currencyId);
            preparedStatement.setBigDecimal(2, price);
            preparedStatement.setLong(3, excludeUserId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) orders.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти встречные заявки", e);
        }
        return orders;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) orders.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти все заявки", e);
        }
        return orders;
    }

    @Override
    public void updateFill(Connection con, Long orderId, BigDecimal filled, OrderStatus status) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_UPDATE_FILL)) {
            preparedStatement.setBigDecimal(1, filled);
            preparedStatement.setString(2, status.name());
            preparedStatement.setLong(3, orderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось обновить заявку с id" + orderId, e);
        }
    }

    @Override
    public Optional<Order> findById(Connection con, Long id) {
        if (id == null) return Optional.empty();

        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_FIND_BY_ID)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка при поиске заявки по id", e);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        return Order.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .currencyId(rs.getLong("currency_id"))
                .side(OrderSide.valueOf(rs.getString("side")))
                .price(rs.getBigDecimal("price"))
                .amount(rs.getBigDecimal("amount"))
                .filled(rs.getBigDecimal("filled"))
                .status(OrderStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .build();
    }
}
