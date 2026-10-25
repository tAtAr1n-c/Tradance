package tradance.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir;

    public FileStorageServiceImpl(String uploadDir) {
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new IllegalStateException("upload.dir не задан в app.properties");
        }
        this.uploadDir = Paths.get(uploadDir);

        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать папку загрузок: " + uploadDir, e);
        }
    }
}
