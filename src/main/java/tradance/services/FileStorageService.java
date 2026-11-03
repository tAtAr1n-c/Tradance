package tradance.services;

import tradance.models.FileInfo;

import javax.servlet.http.Part;
import java.nio.file.Path;
import java.util.Optional;

public interface FileStorageService {
    Long save(Part part, Long userId);
    Optional<FileInfo> findById(Long id);
    Path getPath(String storageName);
}
