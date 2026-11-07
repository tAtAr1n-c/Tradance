package tradance.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Deposit {
    private Long id;
    private Long userId;
    private Long currencyId;
    private Long adminId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
