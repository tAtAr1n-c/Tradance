package tradance.dto;

import lombok.*;
import tradance.models.Role;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionUser implements Serializable {
    private Long id;
    private String username;
    private Role role;
    private String avatarPath;
}
