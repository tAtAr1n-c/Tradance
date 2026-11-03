package tradance.services;

import tradance.exceptions.BusinessException;
import tradance.models.FileInfo;
import tradance.repository.FilesRepository;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir;
    private final FilesRepository filesRepository;
    private static final Map<String, String> TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp");
    private static final long MAX_SIZE = 2 * 1024 * 1024;



    public FileStorageServiceImpl(String uploadDir, FilesRepository filesRepository) {
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new IllegalStateException("upload.dir не задан в app.properties");
        }
        this.uploadDir = Paths.get(uploadDir);
        this.filesRepository = filesRepository;

        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать папку загрузок: " + uploadDir, e);
        }

    }

    @Override
    public Long save(Part part, Long userId) {
        if (part == null || part.getSize() == 0) {
            throw new BusinessException("Выберите файл");
        }
        if (!TYPES.containsKey(part.getContentType())) {
            throw new BusinessException("Только PNG, JPG или WEBP");
        }
        if (part.getSize() > MAX_SIZE) {
            throw new BusinessException("Файл больше 2 МБ");
        }

        String storageName = UUID.randomUUID().toString() + TYPES.get(part.getContentType());

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, uploadDir.resolve(storageName));
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить файл", e);
        }
        FileInfo fileInfo = FileInfo.builder()
                .originalName(part.getSubmittedFileName())
                .storageName(storageName)
                .contentType(part.getContentType())
                .size(part.getSize())
                .userId(userId)
                .build();
        filesRepository.save(fileInfo);
        return fileInfo.getId();
    }

    @Override
    public Optional<FileInfo> findById(Long id) {
        return filesRepository.findById(id);
    }

    @Override
    public Path getPath(String storageName) {
        return uploadDir.resolve(storageName);
    }
}
