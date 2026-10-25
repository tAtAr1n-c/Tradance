package tradance.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tradance.models.RecoveryWord;
import tradance.repository.RecoveryWordsRepository;

import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RecoveryPhraseServiceImpl implements RecoveryPhraseService {

    private final RecoveryWordsRepository recoveryWordsRepository;
    private final List<String> wordlist;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public RecoveryPhraseServiceImpl(RecoveryWordsRepository recoveryWordsRepository, List<String> wordlist) {
        this.wordlist = wordlist;
        this.recoveryWordsRepository = recoveryWordsRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
    }


    @Override
    public List<String> generate() {
        Set<String> words = new LinkedHashSet<>();
        while (words.size() < 10) {
            int index = secureRandom.nextInt(wordlist.size());
            words.add(wordlist.get(index));
        }
        return words.stream().toList();
    }

    @Override
    public List<String> hashAll(List<String> words) {
        return words.stream().map(word -> passwordEncoder.encode(word.trim().toLowerCase())).toList();
    }

    @Override
    public List<String> createFor(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId не может быть null");
        List<String> words = generate();
        recoveryWordsRepository.saveAll(userId, hashAll(words));
        return words;
    }

    @Override
    public List<Integer> pickPositions() {
        Set<Integer> positions = new TreeSet<>();
        while (positions.size() < 3) {
            Integer ind = secureRandom.nextInt(10) + 1;
            positions.add(ind);
        }
        return positions.stream().toList().stream().sorted().toList();
    }

    @Override
    public boolean check(Long userId, List<Integer> positions, List<String> answers) {
        if (userId == null) throw new IllegalArgumentException("userId не может быть null");
        if (answers == null || answers.size() != 3) return false;
        List<RecoveryWord> words = recoveryWordsRepository.findByUserAndPositions(userId, positions);
        if (words.size() != 3) return false;

        for (int i = 0; i < 3; i++) {
            if (!passwordEncoder.matches(answers.get(i).trim().toLowerCase(), words.get(i).getWordHash())) {
                return false;
            }
        }
        return true;
    }
}
