package tradance.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Trade {
    private Long id;
    private Long buyOrderId;
    private Long sellOrderId;
    private Long buyerId;
    private Long sellerId;
    private Long currencyId;
    private BigDecimal price;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
