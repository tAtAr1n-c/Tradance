package tradance.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WalletView {
    private String ticker;
    private String name;
    private Long iconFileId;
    private BigDecimal balance;
    private BigDecimal locked;
    private BigDecimal available;


}
