package tradance.models;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class P2pDeal {
    private Long id;
    private Long offerId;
    private Long buyerId;
    private Long sellerId;
    private Long currencyId;
    private Long paymentMethodId;
    private Long cancelledBy;
    private BigDecimal price;
    private BigDecimal amount;
    private DealStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime closedAt;

}
