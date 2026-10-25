package tradance.repository;

import tradance.models.Trade;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TradesRepositoryJdbcImpl implements TradesRepository {

    private final DataSource dataSource;


    private static final String SQL_FIND_LAST_BY_CURRENCY = "select id, buy_order_id, sell_order_id, buyer_id, seller_id, currency_id, price, amount, created_at from trades where currency_id=? order by created_at desc, id desc limit ?";
    private static final String SQL_FIND_ALL = "select id, buy_order_id, sell_order_id, buyer_id, seller_id, currency_id, price, amount, created_at from trades order by created_at desc";
    private static final String SQL_FIND_LAST_PRICE = "select price from trades where currency_id =? order by created_at desc, id desc limit 1";
    private static final String SQL_FIND_BY_USER = "select id, buy_order_id, sell_order_id, buyer_id, seller_id, currency_id, price, amount, created_at from trades where buyer_id=? or seller_id=? order by created_at desc, id desc";

    private static final String SQL_SAVE = "insert into trades(buy_order_id, sell_order_id, buyer_id, seller_id, currency_id, price, amount) values (?, ?, ?, ?, ?, ?, ?)";


    public TradesRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public List<Trade> findLastByCurrency(Long currencyId, int limit) {
        List<Trade> trades = new ArrayList<>();
        if (currencyId == null || limit <= 0) return trades;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_LAST_BY_CURRENCY)) {
            preparedStatement.setLong(1, currencyId);
            preparedStatement.setInt(2, limit);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) trades.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти сделки по данной монете", e);
        }

        return trades;
    }

    @Override
    public BigDecimal findLastPrice(Long currencyId) {
        if (currencyId == null) return null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_LAST_PRICE)) {

            preparedStatement.setLong(1, currencyId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return resultSet.getBigDecimal("price");
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти последнюю цену на данную монету", e);
        }
        return null;
    }

    @Override
    public List<Trade> findAllByUser(Long userId) {
        List<Trade> trades = new ArrayList<>();
        if (userId == null) return trades;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_USER)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) trades.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти сделки пользователя", e);
        }

        return trades;
    }

    @Override
    public void save(Connection con, Trade trade) {
        if (trade == null) throw new IllegalArgumentException("trade is null");
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_SAVE)) {
            preparedStatement.setLong(1, trade.getBuyOrderId());
            preparedStatement.setLong(2, trade.getSellOrderId());
            preparedStatement.setLong(3, trade.getBuyerId());
            preparedStatement.setLong(4, trade.getSellerId());
            preparedStatement.setLong(5, trade.getCurrencyId());
            preparedStatement.setBigDecimal(6, trade.getPrice());
            preparedStatement.setBigDecimal(7, trade.getAmount());

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new IllegalStateException("Не получилось добавить новую сделку", e);
        }
    }

    @Override
    public List<Trade> findAll() {
        List<Trade> trades = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) trades.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти все сделки", e);
        }
        return trades;
    }


    private Trade mapRow(ResultSet resultSet) throws SQLException {
        return Trade.builder()
                .id(resultSet.getLong("id"))
                .buyOrderId(resultSet.getLong("buy_order_id"))
                .sellOrderId(resultSet.getLong("sell_order_id"))
                .buyerId(resultSet.getLong("buyer_id"))
                .sellerId(resultSet.getLong("seller_id"))
                .currencyId(resultSet.getLong("currency_id"))
                .price(resultSet.getBigDecimal("price"))
                .amount(resultSet.getBigDecimal("amount"))
                .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                .build();
    }
}
