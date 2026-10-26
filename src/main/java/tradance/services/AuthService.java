package tradance.services;

import tradance.dto.*;

import java.util.List;

public interface AuthService {
    List<String> signUp(SignUpForm form);

    PendingLogin startLogin(LoginForm form);

    SessionUser finishLogin(Long userId, List<Integer> positions, PhraseCheckForm form);
//    List<Integer> startRecover(String username);
//    void finishRecover(String username, List<Integer> positions, RecoverForm form);
}
