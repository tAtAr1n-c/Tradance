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

public class User {

    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean blocked;
    private Long avatarFileId;
    private LocalDateTime createdAt;
}
