package tradance.repository;

import tradance.models.OfferStatus;
import tradance.models.P2pOffer;

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

public class P2pOffersRepositoryJdbcImpl implements P2pOffersRepository{
    private final DataSource dataSource;

    private final static String SQL_SAVE = "insert into p2p_offers (user_id, currency_id, payment_method_id, price, amount, remaining, status) values (?, ?, ?, ?, ?, ?, ?) returning id";
    private final static String SQL_FIND_BY_ID = "select id, user_id, currency_id, payment_method_id, price, amount, remaining, status, created_at from p2p_offers where  id=? for update";
    private final static String SQL_FIND_ALL_BY_USER = "select id, user_id, currency_id, payment_method_id, price, amount, remaining, status, created_at from p2p_offers where user_id=? order by created_at desc";
    private final static String SQL_FIND_ACTIVE = "select id, user_id, currency_id, payment_method_id, price, amount, remaining, status, created_at from p2p_offers where status='ACTIVE' and remaining > 0 and currency_id=? order by price, created_at";
    private final static String SQL_UPDATE_REMAINING = "update p2p_offers set remaining=? where id=?";
    private final static String SQL_UPDATE_STATUS = "update p2p_offers set status=? where id=?";
    public P2pOffersRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Connection connection, P2pOffer p2pOffer) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE)){
            preparedStatement.setLong(1, p2pOffer.getUserId());
            preparedStatement.setLong(2, p2pOffer.getCurrencyId());
            preparedStatement.setLong(3, p2pOffer.getPaymentMethodId());
            preparedStatement.setBigDecimal(4, p2pOffer.getPrice());
            preparedStatement.setBigDecimal(5, p2pOffer.getAmount());
            preparedStatement.setBigDecimal(6, p2pOffer.getRemaining());
            preparedStatement.setString(7, p2pOffer.getStatus().name());
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    p2pOffer.setId(resultSet.getLong("id"));
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось добавить предложение", e);
        }
    }

    @Override
    public Optional<P2pOffer> findById(Connection connection, Long id) {
        if(id == null) return Optional.empty();
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_ID)){
            preparedStatement.setLong(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти предложение с айди: " + id, e);
        }
    }

    @Override
    public List<P2pOffer> findActive(Long currencyId) {
        List<P2pOffer> actives = new ArrayList<>();
        if(currencyId == null) return actives;
        try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ACTIVE)){
            preparedStatement.setLong(1, currencyId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    actives.add(mapRow(resultSet));
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти предложения у монеты по id: " + currencyId, e);
        }
        return actives;
    }

    @Override
    public List<P2pOffer> findAllByUser(Long userId) {
        List<P2pOffer> p2pOffers = new ArrayList<>();
        if(userId == null) return p2pOffers;
        try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL_BY_USER)){
            preparedStatement.setLong(1, userId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    p2pOffers.add(mapRow(resultSet));
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти предложений у пользователя с айди: " + userId, e);
        }
        return p2pOffers;
    }

    @Override
    public void updateRemaining(Connection connection, Long id, BigDecimal remaining) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_REMAINING)){
            preparedStatement.setLong(2, id);
            preparedStatement.setBigDecimal(1, remaining);
            preparedStatement.executeUpdate();
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось обновить остаток по предложению с id: " + id, e);
        }
    }

    @Override
    public void updateStatus(Connection connection, Long id, OfferStatus offerStatus) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_STATUS)){
            preparedStatement.setLong(2, id);
            preparedStatement.setString(1, offerStatus.name());
            preparedStatement.executeUpdate();
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось обновить статус у предложения с id: " + id, e);
        }
    }

    private P2pOffer mapRow(ResultSet resultSet) throws SQLException{
        return P2pOffer.builder()
                .id(resultSet.getLong("id"))
                .userId(resultSet.getLong("user_id"))
                .currencyId(resultSet.getLong("currency_id"))
                .paymentMethodId(resultSet.getLong("payment_method_id"))
                .price(resultSet.getBigDecimal("price"))
                .amount(resultSet.getBigDecimal("amount"))
                .remaining(resultSet.getBigDecimal("remaining"))
                .status(OfferStatus.valueOf(resultSet.getString("status")))
                .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                .build();
    }
}
