package tradance.repository;

import tradance.models.RecoveryWord;

import java.util.List;

public interface RecoveryWordsRepository {
    void saveAll(Long userId, List<String> hashes);

    List<RecoveryWord> findByUserAndPositions(Long userId, List<Integer> positions);
}
