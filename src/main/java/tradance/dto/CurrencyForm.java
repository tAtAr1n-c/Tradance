package tradance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CurrencyForm {
    private String ticker;
    private String name;
    private String quote;
    private String active;
}
