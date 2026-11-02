package tradance.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Currency {
    private Long id;
    private String ticker;
    private String name;
    private Long iconFileId;
    private boolean quote;
    private boolean active;
}
