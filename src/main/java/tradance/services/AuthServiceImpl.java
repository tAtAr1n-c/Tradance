package tradance.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tradance.dto.LoginForm;
import tradance.dto.PhraseCheckForm;
import tradance.dto.SessionUser;
import tradance.dto.SignUpForm;
import tradance.exceptions.ValidationException;
import tradance.models.Currency;
import tradance.models.Role;
import tradance.models.User;
import tradance.models.Wallet;
import tradance.repository.CurrenciesRepository;
import tradance.repository.UsersRepository;
import tradance.repository.WalletsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AuthServiceImpl implements AuthService {


    private DataSource dataSource;
    private UsersRepository usersRepository;
    private WalletsRepository walletsRepository;
    private CurrenciesRepository currenciesRepository;
    private RecoveryPhraseService recoveryPhraseService;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(DataSource dataSource, UsersRepository usersRepository, WalletsRepository walletsRepository, CurrenciesRepository currenciesRepository, RecoveryPhraseService recoveryPhraseService) {
        this.dataSource = dataSource;
        this.usersRepository = usersRepository;
        this.walletsRepository = walletsRepository;
        this.currenciesRepository = currenciesRepository;
        this.recoveryPhraseService = recoveryPhraseService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public List<String> signUp(SignUpForm form) {
        Map<String, String> errors = new TreeMap<>();

        String username = form.getUsername();
        String email = form.getEmail();
        String password = form.getPassword();
        String passwordRepeat = form.getPasswordRepeat();
        if (username.isBlank() || !username.matches("[A-Za-z0-9_]{3,30}"))
            errors.put("username", "Логин от 3 до 30 символов и может содержать буквы,цифры,_");
        if (email == null || email.isBlank() || !email.matches("[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+"))
            errors.put("email", "");
        if (password.length() < 8 || password.length() > 64) errors.put("password", "Пароль от 8 до 64");
        if (!passwordRepeat.equals(password)) errors.put("passwordRepeat", "Пароли не совпадают");
        if (usersRepository.existsByEmail(email)) errors.put("EmailRepeat", "Email уже занят");
        if (usersRepository.existsByUsername(username)) errors.put("LoginRepeat", "Логин уже занят");
        if (!errors.isEmpty()) throw new ValidationException(errors);

        User user = User.builder()
                .username(form.getUsername())
                .email(form.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();
        usersRepository.save(user);

        Currency usdt = currenciesRepository.findQuote();

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            Wallet wallet = walletsRepository.findOrCreate(connection, user.getId(), usdt.getId());
            walletsRepository.credit(connection, user.getId(), new BigDecimal("10000"));
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw new IllegalStateException("Не удалось создать стартовый кошелёк", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return recoveryPhraseService.createFor(user.getId());
    }

    @Override
    public List<Integer> startLogin(LoginForm form) {
        return List.of();
    }

    @Override
    public SessionUser finishLogin(Long userId, List<Integer> positions, PhraseCheckForm form) {
        return null;
    }

//    @Override
//    public List<Integer> startRecover(String username) {
//        return List.of();
//    }
//
//    @Override
//    public void finishRecover(String username, List<Integer> positions, RecoverForm form) {
//
    //}
}
