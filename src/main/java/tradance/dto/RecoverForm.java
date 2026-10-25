package tradance.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoverForm {
    private List<String> words;
    private String newPassword;
    private String newPasswordRepeat;
}
