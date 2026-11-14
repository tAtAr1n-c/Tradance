package tradance.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class P2pOfferForm {
    private String currency;
    private String paymentMethodId;
    private String price;
    private String amount;
}
