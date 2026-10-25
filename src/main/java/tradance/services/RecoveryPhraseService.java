package tradance.services;

import java.util.List;

public interface RecoveryPhraseService {
    List<String> generate();

    List<String> hashAll(List<String> words);

    List<String> createFor(Long userId);

    List<Integer> pickPositions();

    boolean check(Long userId, List<Integer> positions, List<String> answers);
}
