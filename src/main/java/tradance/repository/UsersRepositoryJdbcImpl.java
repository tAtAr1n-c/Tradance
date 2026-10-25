package tradance.repository;

import tradance.models.Role;
import tradance.models.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsersRepositoryJdbcImpl implements UsersRepository {

    private final DataSource dataSource;


    private static final String SQL_FIND_BY_USERNAME = "select id, username, email, password_hash, role, is_blocked, avatar_path, created_at from users where username=?";
    private static final String SQL_FIND_BY_EMAIL = "select id, username, email, password_hash, role, is_blocked, avatar_path, created_at from users where email=?";
    private static final String SQL_FIND_BY_ID = "select id, username, email, password_hash, role, is_blocked, avatar_path, created_at from users where id=?";
    private static final String SQL_FIND_ALL = "select id, username, email, password_hash, role, is_blocked, avatar_path, created_at from users";

    private static final String SQL_DELETE = "delete from users where id=?";

    private static final String SQL_UPDATE_BLOCKED = "update users set is_blocked=? where id=?";
    private static final String SQL_UPDATE_AVATAR = "update users set avatar_path=? where id=?";
    private static final String SQL_UPDATE_PASSWORD = "update users set password_hash=? where id=?";

    private static final String SQL_SAVE_USER = "insert into users (username, email, password_hash, role) values (?, ?, ?, ?) returning id";
    private static final String SQL_UPDATE_USER = "update users set username=?, email=?, role=?, is_blocked=?, avatar_path=? where id=?";

    private static final String SQL_EXISTS_BY_USERNAME = "select exists(select 1 from users where username=?)";
    private static final String SQL_EXISTS_BY_EMAIL = "select exists(select 1 from users where email=?)";


    public UsersRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();

        return findOne(SQL_FIND_BY_USERNAME, username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return findOne(SQL_FIND_BY_EMAIL, email);
    }

    @Override
    public void updateBlocked(Long id, boolean blocked) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_BLOCKED)) {
            preparedStatement.setBoolean(1, blocked);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось изменить блокировку пользователя", e);
        }

    }

    @Override
    public void updateAvatar(Long id, String path) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_AVATAR)) {
            preparedStatement.setString(1, path);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось изменить аватар пользователя", e);
        }
    }

    @Override
    public void updatePasswordHash(Long id, String hash) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_PASSWORD)) {
            preparedStatement.setString(1, hash);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось изменить пароль пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(SQL_FIND_BY_ID, id);
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_ALL)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) users.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти всех пользователей", e);
        }
        return users;
    }

    @Override
    public boolean existsByUsername(String username) {
        boolean flag = false;
        if (username == null || username.isBlank()) return flag;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EXISTS_BY_USERNAME)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) flag = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка проверки username", e);
        }
        return flag;

    }

    @Override
    public boolean existsByEmail(String email) {
        boolean flag = false;
        if (email == null || email.isBlank()) return flag;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_EXISTS_BY_EMAIL)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) flag = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка проверки email", e);
        }
        return flag;
    }

    @Override
    public void save(User entity) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE_USER)) {
            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getEmail());
            preparedStatement.setString(3, entity.getPasswordHash());
            preparedStatement.setString(4, entity.getRole().name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    entity.setId(resultSet.getLong("id"));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Не получилось добавить пользователя", e);
        }
    }

    @Override
    public void update(User entity) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_USER)) {
            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getEmail());
            preparedStatement.setString(3, entity.getRole().name());
            preparedStatement.setBoolean(4, entity.isBlocked());
            preparedStatement.setString(5, entity.getAvatarPath());
            preparedStatement.setLong(6, entity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось изменить пользователя с id" + entity.getId(), e);
        }
    }

    public void delete(Long id) {
        if (id == null) return;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось удалить пользователя", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .role(Role.valueOf(rs.getString("role")))
                .blocked(rs.getObject("is_blocked", Boolean.class))
                .avatarPath(rs.getString("avatar_path"))
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .build();
    }

    private Optional<User> findOne(String sql, Object param) {
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
            throw new IllegalStateException("Ошибка при поиске пользователя по параметру", e);
        }
    }
}
