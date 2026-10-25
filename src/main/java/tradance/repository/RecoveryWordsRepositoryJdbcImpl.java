package tradance.repository;

import tradance.models.RecoveryWord;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecoveryWordsRepositoryJdbcImpl implements RecoveryWordsRepository {

    private static final String SQL_SAVE_ALL = "insert into recovery_words(user_id, position, word_hash) values ";

    private static final String SQL_FIND_WORDS_BY_ID_USER = "select id, user_id, position, word_hash from recovery_words where user_id = ? and position in (?, ?, ?) order by position";

    private final DataSource dataSource;

    public RecoveryWordsRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveAll(Long userId, List<String> hashes) {

        String placeHolder = hashes.stream().map(hash -> "(?, ?, ?)").collect(Collectors.joining(", "));
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE_ALL + placeHolder)) {
            int i = 1;

            int pos = 1;
            for (String hash : hashes) {
                preparedStatement.setLong(i++, userId);
                preparedStatement.setInt(i++, pos++);
                preparedStatement.setString(i++, hash);

            }

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось добавить проверочный слова", e);
        }
    }

    @Override
    public List<RecoveryWord> findByUserAndPositions(Long userId, List<Integer> positions) {
        List<RecoveryWord> words = new ArrayList<>();
        if (userId == null || positions == null || positions.size() != 3) return words;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_WORDS_BY_ID_USER)) {
            preparedStatement.setLong(1, userId);
            for (int i = 0; i < positions.size(); i++) {
                preparedStatement.setInt(i + 2, positions.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    words.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти проверочные слова пользователя", e);
        }
        return words;
    }

    private RecoveryWord mapRow(ResultSet rs) throws SQLException {
        return RecoveryWord.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .position(rs.getInt("position"))
                .wordHash(rs.getString("word_hash"))
                .build();
    }
}
