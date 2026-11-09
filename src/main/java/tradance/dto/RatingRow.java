package tradance.dto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RatingRow {
    private Long userId;
    private String username;
    private Long avatarFileId;
    private BigDecimal portfolio;
    private BigDecimal invested;
    private BigDecimal profit;
    private BigDecimal profitPct;
    private int tradesCount;
}
