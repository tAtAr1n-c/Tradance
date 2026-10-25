package tradance.services;

import tradance.dto.LoginForm;
import tradance.dto.PhraseCheckForm;
import tradance.dto.SessionUser;
import tradance.dto.SignUpForm;

import java.util.List;

public interface AuthService {
    List<String> signUp(SignUpForm form);

    List<Integer> startLogin(LoginForm form);

    SessionUser finishLogin(Long userId, List<Integer> positions, PhraseCheckForm form);
//    List<Integer> startRecover(String username);
//    void finishRecover(String username, List<Integer> positions, RecoverForm form);
}
