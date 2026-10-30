package tradance.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@ToString
@EqualsAndHashCode
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private Long currencyId;
    private OrderSide side;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal filled;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public BigDecimal getRemaining() {
        return amount.subtract(filled);
    }

}
