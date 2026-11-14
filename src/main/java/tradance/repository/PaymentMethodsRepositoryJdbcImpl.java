package tradance.repository;

import tradance.models.PaymentMethod;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentMethodsRepositoryJdbcImpl implements PaymentMethodsRepository{

    private final DataSource dataSource;

    private static final String SQL_FIND_ALL = "select id, name from payment_methods order by id";
    private static final String SQL_FIND_BY_ID = "select id, name from payment_methods where id=?";

    public PaymentMethodsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<PaymentMethod> findAll() {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL);
        ){
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    paymentMethods.add(PaymentMethod.builder()
                            .id(resultSet.getLong("id"))
                            .name(resultSet.getString("name"))
                            .build());
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти способы оплаты", e);
        }
        return paymentMethods;
    }

    @Override
    public Optional<PaymentMethod> findById(Long id) {
        if(id == null) return Optional.empty();
        try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_ID)){
            preparedStatement.setLong(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(PaymentMethod.builder().id(resultSet.getLong("id")).name(resultSet.getString("name")).build());
                }
                return Optional.empty();
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти способ оплаты под айди: " + id, e);
        }
    }
}
