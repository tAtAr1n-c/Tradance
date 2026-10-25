package tradance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpForm {
    private String username;
    private String email;
    private String password;
    private String passwordRepeat;
}
