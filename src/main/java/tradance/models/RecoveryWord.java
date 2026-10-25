package tradance.models;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class RecoveryWord {
    private Long id;
    private Long userId;
    private int position;
    private String wordHash;
}
