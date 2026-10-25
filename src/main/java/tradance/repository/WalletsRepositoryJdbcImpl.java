package tradance.repository;

import tradance.models.Wallet;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WalletsRepositoryJdbcImpl implements WalletsRepository {

    private final DataSource dataSource;


    private static final String SQL_FIND_BY_ID = "select id, user_id, currency_id, balance, locked from wallets where id=?";
    private static final String SQL_FIND_ALL_BY_USER = "select id, user_id, currency_id, balance, locked from wallets where user_id=? order by currency_id";
    private static final String SQL_FIND_BY_USER_AND_CURRENCY = "select id, user_id, currency_id, balance, locked from wallets where user_id=? and currency_id=?";

    private static final String SQL_LOCK_FUNDS = "update wallets set locked = locked + ? where id=? and balance - locked >= ?";
    private static final String SQL_UNLOCK = "update wallets set locked = locked - ? where id=?";
    private static final String SQL_SPEND_LOCKED = "update wallets set balance = balance - ?, locked = locked - ? where id=?";
    private static final String SQL_CREDIT = "update wallets set balance = balance + ? where id=?";

    private static final String SQL_CREATE_IF_ABSENT = "insert into wallets (user_id, currency_id) values (?, ?) on conflict (user_id, currency_id) do nothing";
    private static final String SQL_FIND_BY_USER_AND_CURRENCY_FOR_UPDATE = "select id, user_id, currency_id, balance, locked from wallets where user_id=? and currency_id=? for update";


    public WalletsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        if (id == null) return Optional.empty();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_ID)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return Optional.of(mapRow(resultSet));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти кошелек по айди", e);
        }
    }

    @Override
    public List<Wallet> findAllByUser(Long userId) {
        List<Wallet> wallets = new ArrayList<>();
        if (userId == null) return wallets;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL_BY_USER)) {
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) wallets.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти кошельки пользователя", e);
        }
        return wallets;
    }

    @Override
    public Optional<Wallet> findByUserAndCurrency(Long userId, Long currencyId) {
        if (userId == null || currencyId == null) return Optional.empty();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_BY_USER_AND_CURRENCY)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, currencyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return Optional.of(mapRow(resultSet));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти кошелек пользователя в валюте", e);
        }
    }

    @Override
    public boolean lockFunds(Connection con, Long walletId, BigDecimal amount) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_LOCK_FUNDS)) {
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setLong(2, walletId);
            preparedStatement.setBigDecimal(3, amount);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось заморозить средства", e);
        }
    }

    @Override
    public void unlock(Connection con, Long walletId, BigDecimal amount) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_UNLOCK)) {
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setLong(2, walletId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось разморозить средства", e);
        }
    }

    @Override
    public void spendLocked(Connection con, Long walletId, BigDecimal spend, BigDecimal unlock) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_SPEND_LOCKED)) {
            preparedStatement.setBigDecimal(1, spend);
            preparedStatement.setBigDecimal(2, unlock);
            preparedStatement.setLong(3, walletId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось списать замороженные средства", e);
        }
    }

    @Override
    public void credit(Connection con, Long walletId, BigDecimal amount) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_CREDIT)) {
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setLong(2, walletId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось начислить средства", e);
        }
    }

    @Override
    public Wallet findOrCreate(Connection con, Long userId, Long currencyId) {
        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_CREATE_IF_ABSENT)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, currencyId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось создать кошелек", e);
        }

        try (PreparedStatement preparedStatement = con.prepareStatement(SQL_FIND_BY_USER_AND_CURRENCY_FOR_UPDATE)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, currencyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти кошелек", e);
        }
        throw new IllegalStateException("Кошелек не найден после создания: userId=" + userId + ", currencyId=" + currencyId);
    }

    private Wallet mapRow(ResultSet resultSet) throws SQLException {
        return Wallet.builder()
                .id(resultSet.getLong("id"))
                .userId(resultSet.getLong("user_id"))
                .currencyId(resultSet.getLong("currency_id"))
                .balance(resultSet.getBigDecimal("balance"))
                .locked(resultSet.getBigDecimal("locked"))
                .build();
    }
}
