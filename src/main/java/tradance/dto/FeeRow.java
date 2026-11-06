package tradance.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FeeRow {
    private Long currencyId;
    private Long tradesCount;
    private BigDecimal turnover;
    private BigDecimal fee;
}
