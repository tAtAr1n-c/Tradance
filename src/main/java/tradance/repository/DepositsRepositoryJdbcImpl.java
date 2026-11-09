package tradance.repository;

import tradance.models.Deposit;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DepositsRepositoryJdbcImpl implements DepositsRepository {

    private final DataSource dataSource;

    public DepositsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String SQL_FIND_ALL = "select id, user_id, currency_id, admin_id, amount, created_at from deposits order by created_at desc";
    private static final String SQL_SAVE = "insert into deposits (user_id, currency_id, admin_id, amount) values (?, ?, ?, ?) returning id";

    @Override
    public void save(Connection connection, Deposit deposit) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE)){
            preparedStatement.setLong(1, deposit.getUserId());
            preparedStatement.setLong(2, deposit.getCurrencyId());
            preparedStatement.setLong(3, deposit.getAdminId());
            preparedStatement.setBigDecimal(4, deposit.getAmount());

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    deposit.setId(resultSet.getLong("id"));
                }
            }
        }catch (SQLException e) {
            throw new IllegalStateException("Не получилось сохранить депозит", e);
        }
    }

    @Override
    public List<Deposit> findAll() {
        List<Deposit> deposits = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL)){
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    deposits.add(Deposit.builder()
                            .id(resultSet.getLong("id"))
                            .userId(resultSet.getLong("user_id"))
                            .currencyId(resultSet.getLong("currency_id"))
                            .adminId(resultSet.getLong("admin_id"))
                            .amount(resultSet.getBigDecimal("amount"))
                            .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                            .build());
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти все депозиты", e);
        }
        return deposits;
    }
}
