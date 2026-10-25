package tradance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderForm {
    private String currency;
    private String side;
    private String price;
    private String amount;
}
