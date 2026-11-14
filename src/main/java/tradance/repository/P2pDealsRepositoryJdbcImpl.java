package tradance.repository;

import tradance.models.DealStatus;
import tradance.models.P2pDeal;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class P2pDealsRepositoryJdbcImpl implements P2pDealsRepository{

    private final static String SQL_SELECT = "select d.id, d.offer_id, d.buyer_id, d.cancelled_by, d.amount, d.status, d.created_at, d.paid_at, d.closed_at, o.user_id as seller_id, o.currency_id, o.payment_method_id, o.price from p2p_deals d join p2p_offers o on o.id = d.offer_id";
    private final static String SQL_FIND_BY_ID = SQL_SELECT + " where d.id=?";
    private final static String SQL_FIND_BY_ID_FOR_UPDATE = SQL_FIND_BY_ID + " for update";
    private final static String SQL_FIND_ALL_BY_USER = SQL_SELECT + " where d.buyer_id=? or o.user_id=? order by d.created_at desc";
    private final static String SQL_SAVE = "insert into p2p_deals (offer_id, buyer_id, amount, status) values (?, ?, ?, ?) returning id";
    private final static String SQL_MARK_PAID = "update p2p_deals set status='PAID', paid_at=now() where id=?";
    private final static String SQL_CLOSE = "update p2p_deals set status=?, closed_at=now(), cancelled_by=? where id=?";

    private final DataSource dataSource;

    public P2pDealsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Connection connection, P2pDeal p2pDeal) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE)){
            preparedStatement.setLong(1, p2pDeal.getOfferId());
            preparedStatement.setLong(2, p2pDeal.getBuyerId());
            preparedStatement.setBigDecimal(3, p2pDeal.getAmount());
            preparedStatement.setString(4, p2pDeal.getStatus().name());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    p2pDeal.setId(resultSet.getLong("id"));
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось сохранить сделку", e);
        }
    }

    @Override
    public Optional<P2pDeal> findById(Connection connection, Long id) {
        if(id == null) return Optional.empty();
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_ID_FOR_UPDATE)){
            preparedStatement.setLong(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }catch (SQLException e){
            throw new IllegalStateException("Не удалось найти сделку с айди: " + id, e);
        }
    }

    @Override
    public Optional<P2pDeal> findById(Long id) {
        if(id == null) return Optional.empty();
        try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_ID)){
            preparedStatement.setLong(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }catch (SQLException e){
            throw new IllegalStateException("Не удалось найти сделку с айди: " + id, e);
        }
    }

    @Override
    public List<P2pDeal> findAllByUserId(Long userId) {
        List<P2pDeal> p2pDeals = new ArrayList<>();
        if(userId == null) return p2pDeals;
        try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL_BY_USER)){
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, userId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    p2pDeals.add(mapRow(resultSet));
                }
            }
        }catch (SQLException e){
            throw new IllegalStateException("Не удалось найти сделки у пользователя с id: " + userId, e);
        }
        return p2pDeals;
    }

    @Override
    public void markPaid(Connection connection, Long id) {
        if(id == null) throw new IllegalStateException("Не удалось обновить метку оплаты");
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_MARK_PAID)){
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось обновить метку оплаты", e);
        }
    }

    @Override
    public void close(Connection connection, Long id, DealStatus dealStatus, Long cancelledBy) {
        if(id == null) throw new IllegalStateException("Не удалось закрыть сделку");
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_CLOSE)){
            preparedStatement.setString(1, dealStatus.name());
            preparedStatement.setObject(2, cancelledBy);
            preparedStatement.setLong(3, id);
            preparedStatement.executeUpdate();
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось закрыть сделку с id: " + id, e);
        }
    }


    private P2pDeal mapRow(ResultSet resultSet) throws SQLException {
        return P2pDeal.builder()
                .id(resultSet.getLong("id"))
                .offerId(resultSet.getLong("offer_id"))
                .buyerId(resultSet.getLong("buyer_id"))
                .sellerId(resultSet.getLong("seller_id"))
                .currencyId(resultSet.getLong("currency_id"))
                .paymentMethodId(resultSet.getLong("payment_method_id"))
                .cancelledBy(resultSet.getObject("cancelled_by", Long.class))
                .price(resultSet.getBigDecimal("price"))
                .amount(resultSet.getBigDecimal("amount"))
                .status(DealStatus.valueOf(resultSet.getString("status")))
                .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                .paidAt(resultSet.getObject("paid_at", LocalDateTime.class))
                .closedAt(resultSet.getObject("closed_at", LocalDateTime.class))
                .build();
    }
}
