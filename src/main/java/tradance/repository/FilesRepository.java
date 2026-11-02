package tradance.repository;

import tradance.models.FileInfo;

import java.util.Optional;

public interface FilesRepository {
    void save(FileInfo file);
    Optional<FileInfo> findById(Long id);
}
