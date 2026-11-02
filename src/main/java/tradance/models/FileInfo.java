package tradance.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class FileInfo {
    private Long id;
    private String originalName;
    private String storageName;
    private String contentType;
    private Long size;
    private Long userId;
    private LocalDateTime createdAt;
}
