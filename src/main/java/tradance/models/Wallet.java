package tradance.models;

import lombok.*;


import java.math.BigDecimal;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Wallet {
    private Long id;
    private Long userId;
    private Long currencyId;
    private BigDecimal balance;
    private BigDecimal locked;


    public BigDecimal getAvailable() {
        return balance.subtract(locked);
    }

}
