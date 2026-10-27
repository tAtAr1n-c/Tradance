package tradance.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MarketRow {
    private Long currencyId;
    private String ticker;
    private String name;
    private String iconPath;
    private BigDecimal lastPrice;
    private String trend;
    private boolean favorite;
}
