package tradance.repository;

import tradance.models.FileInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class FilesRepositoryJdbcImpl implements FilesRepository {

    private final DataSource dataSource;

    public FilesRepositoryJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String SQL_SAVE_FILE = "insert into files (original_name, storage_name, content_type, size, user_id) values (?, ?, ?, ?, ?) returning id";
    private static final String SQL_FIND_BY_ID = "select id, original_name, storage_name, content_type, size, user_id, created_at from files where id=?";
    @Override
    public void save(FileInfo file) {
        try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE_FILE)){
            preparedStatement.setString(1, file.getOriginalName());
            preparedStatement.setString(2, file.getStorageName());
            preparedStatement.setString(3, file.getContentType());
            preparedStatement.setLong(4, file.getSize());
            preparedStatement.setLong(5, file.getUserId());

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    file.setId(resultSet.getLong(1));
                }
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не получилось добавить файл", e);
        }
    }

    @Override
    public Optional<FileInfo> findById(Long id) {
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
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось найти файл под id: " + id, e);
        }
    }

    private FileInfo mapRow(ResultSet resultSet) throws SQLException {
        return FileInfo.builder()
                .id(resultSet.getLong(1))
                .originalName(resultSet.getString(2))
                .storageName(resultSet.getString(3))
                .contentType(resultSet.getString(4))
                .size(resultSet.getLong(5))
                .userId(resultSet.getLong(6))
                .createdAt(resultSet.getObject(7, LocalDateTime.class))
                .build();
    }
}
