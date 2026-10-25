package tradance.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApiResponse {
    private boolean ok;
    private String error;
    private Map<String, String> fieldErrors;
    private Object data;
}
