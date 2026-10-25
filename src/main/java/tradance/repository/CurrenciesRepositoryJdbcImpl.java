package tradance.repository;

import tradance.models.Currency;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CurrenciesRepositoryJdbcImpl implements CurrenciesRepository {

    private final DataSource dataSource;


    private static final String SQL_FIND_BY_TICKER = "select id, ticker, name, icon_path, is_quote, is_active from currencies where ticker=?";
    private static final String SQL_FIND_BY_ID = "select id, ticker, name, icon_path, is_quote, is_active from currencies where id=?";
    private static final String SQL_FIND_ALL = "select id, ticker, name, icon_path, is_quote, is_active from currencies order by id";
    private static final String SQL_FIND_ALL_ACTIVE_TRADABLE = "select id, ticker, name, icon_path, is_quote, is_active from currencies where is_active=true and is_quote=false order by ticker";
    private static final String SQL_FIND_QUOTE = "select id, ticker, name, icon_path, is_quote, is_active from currencies where is_quote=true";

    private static final String SQL_DELETE = "delete from currencies where id=?";

    private static final String SQL_SAVE_CURRENCY = "insert into currencies (ticker, name, icon_path, is_quote, is_active) values (?, ?, ?, ?, ?) returning id";
    private static final String SQL_UPDATE_CURRENCY = "update currencies set ticker=?, name=?, icon_path=?, is_quote=?, is_active=? where id=?";

    private static final String SQL_IS_USED = "select exists(select 1 from wallets where currency_id=?) or exists(select 1 from orders where currency_id=?)";

    private static final String SQL_FIND_FAVORITE_IDS = "select currency_id from favorites where user_id=?";
    private static final String SQL_ADD_FAVORITE = "insert into favorites (user_id, currency_id) values (?, ?) on conflict do nothing";
    private static final String SQL_REMOVE_FAVORITE = "delete from favorites where user_id=? and currency_id=?";
    private static final String SQL_IS_FAVORITE = "select exists(select 1 from favorites where user_id=? and currency_id=?)";


    public CurrenciesRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Optional<Currency> findByTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) return Optional.empty();
        return findOne(SQL_FIND_BY_TICKER, ticker);
    }

    @Override
    public List<Currency> findAllActiveTradable() {
        return findMany(SQL_FIND_ALL_ACTIVE_TRADABLE);
    }

    @Override
    public Currency findQuote() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_QUOTE)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return mapRow(resultSet);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка при поиске quote-валюты", e);
        }
        throw new IllegalStateException("Quote-валюта не найдена (нужна строка с is_quote=true в currencies)");
    }

    @Override
    public boolean isUsed(Long currencyId) {
        boolean flag = false;
        if (currencyId == null) return flag;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_IS_USED)) {
            preparedStatement.setLong(1, currencyId);
            preparedStatement.setLong(2, currencyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) flag = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка проверки использования валюты", e);
        }
        return flag;
    }

    @Override
    public Set<Long> findFavoriteIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        if (userId == null) return ids;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_FAVORITE_IDS)) {
            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) ids.add(resultSet.getLong("currency_id"));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти избранные валюты пользователя", e);
        }
        return ids;
    }

    @Override
    public void addFavorite(Long userId, Long currencyId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_ADD_FAVORITE)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, currencyId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось добавить валюту в избранное", e);
        }
    }

    @Override
    public void removeFavorite(Long userId, Long currencyId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_REMOVE_FAVORITE)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, currencyId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось убрать валюту из избранного", e);
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long currencyId) {
        boolean flag = false;
        if (userId == null || currencyId == null) return flag;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_IS_FAVORITE)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, currencyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) flag = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка проверки избранного", e);
        }
        return flag;
    }

    @Override
    public Optional<Currency> findById(Long id) {
        return findOne(SQL_FIND_BY_ID, id);
    }

    @Override
    public List<Currency> findAll() {
        return findMany(SQL_FIND_ALL);
    }

    @Override
    public void save(Currency entity) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE_CURRENCY)) {
            preparedStatement.setString(1, entity.getTicker());
            preparedStatement.setString(2, entity.getName());
            preparedStatement.setString(3, entity.getIconPath());
            preparedStatement.setBoolean(4, entity.isQuote());
            preparedStatement.setBoolean(5, entity.isActive());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    entity.setId(resultSet.getLong("id"));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не получилось добавить валюту", e);
        }
    }

    @Override
    public void update(Currency entity) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_CURRENCY)) {
            preparedStatement.setString(1, entity.getTicker());
            preparedStatement.setString(2, entity.getName());
            preparedStatement.setString(3, entity.getIconPath());
            preparedStatement.setBoolean(4, entity.isQuote());
            preparedStatement.setBoolean(5, entity.isActive());
            preparedStatement.setLong(6, entity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось изменить валюту с id" + entity.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) return;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось удалить валюту", e);
        }
    }

    private Currency mapRow(ResultSet rs) throws SQLException {
        return Currency.builder()
                .id(rs.getLong("id"))
                .ticker(rs.getString("ticker"))
                .name(rs.getString("name"))
                .iconPath(rs.getString("icon_path"))
                .quote(rs.getBoolean("is_quote"))
                .active(rs.getBoolean("is_active"))
                .build();
    }

    private Optional<Currency> findOne(String sql, Object param) {
        if (param == null) return Optional.empty();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setObject(1, param);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка при поиске валюты по параметру", e);
        }
    }

    private List<Currency> findMany(String sql) {
        List<Currency> currencies = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) currencies.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти валюты", e);
        }
        return currencies;
    }
}
