package tradance.models;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class P2pOffer {
    private Long id;
    private Long userId;
    private Long currencyId;
    private Long paymentMethodId;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal remaining;
    private OfferStatus status;
    private LocalDateTime createdAt;
}
