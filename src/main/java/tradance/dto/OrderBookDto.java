package tradance.dto;

import lombok.*;
import tradance.models.Currency;
import tradance.models.Trade;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderBookDto {
    private List<OrderBookLevel> bids;
    private List<OrderBookLevel> asks;
    private BigDecimal lastPrice;
    private List<Trade> lastTrades;
    private Currency currency;
}
