package tradance.repository;

import tradance.models.Deposit;

import java.sql.Connection;
import java.util.List;

public interface DepositsRepository {
    void save(Connection connection, Deposit deposit);
    List<Deposit> findAll();
}
