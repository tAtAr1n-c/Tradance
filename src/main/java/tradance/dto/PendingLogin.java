package tradance.dto;


import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PendingLogin implements Serializable {
    private Long userId;
    private List<Integer> positions;
}
