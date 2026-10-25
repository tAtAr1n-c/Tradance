package tradance.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderBookLevel {
    private BigDecimal price;
    private BigDecimal amount;
}
