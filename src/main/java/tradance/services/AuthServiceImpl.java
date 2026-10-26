package tradance.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tradance.dto.*;
import tradance.exceptions.BusinessException;
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
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class AuthServiceImpl implements AuthService {


    private final DataSource dataSource;
    private final UsersRepository usersRepository;
    private final WalletsRepository walletsRepository;
    private final CurrenciesRepository currenciesRepository;
    private final RecoveryPhraseService recoveryPhraseService;

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
        String email = form.getEmail().toLowerCase();
        String password = form.getPassword();
        String passwordRepeat = form.getPasswordRepeat();
        if (username == null || username.isBlank() || !username.matches("[A-Za-z0-9_]{3,30}"))
            errors.put("username", "Логин от 3 до 30 символов и может содержать буквы,цифры,_");
        if (email == null || email.isBlank() || !email.matches("[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+"))
            errors.put("email", "Некорректный email");
        if (password == null ||  password.length() < 8 || password.length() > 64) errors.put("password", "Пароль от 8 до 64");
        if (passwordRepeat == null ||!passwordRepeat.equals(password)) errors.put("passwordRepeat", "Пароли не совпадают");
        if (!errors.containsKey("email") && usersRepository.existsByEmail(email)) {
            errors.put("email", "Email уже занят");
        }
        if (!errors.containsKey("username") && usersRepository.existsByUsername(username)) errors.put("username", "Логин уже занят");
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
            walletsRepository.credit(connection, wallet.getId(), new BigDecimal("10000"));
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
    public PendingLogin startLogin(LoginForm form) {
        if(form.getUsername() == null || form.getPassword() == null) {
            throw new BusinessException("Неверный логин или пароль");
        }
        User user = usersRepository.findByUsername(form.getUsername()).orElseThrow(() -> new BusinessException("Неверный логин или пароль"));
        if(!passwordEncoder.matches(form.getPassword(), user.getPasswordHash())){
            throw new BusinessException("Неверный логин или пароль");
        }
        if(user.isBlocked()) throw new BusinessException("Пользователь заблокирован");

        return PendingLogin.builder()
                .userId(user.getId())
                .positions(recoveryPhraseService.pickPositions())
                .build();
    }

    @Override
    public SessionUser finishLogin(Long userId, List<Integer> positions, PhraseCheckForm form) {
        if(!recoveryPhraseService.check(userId, positions, form.getWords())) throw new BusinessException("Слова не совпадают");
        User user = usersRepository.findById(userId).orElseThrow(() -> new BusinessException("Пользователь не найден"));
        if(user.isBlocked()) throw new BusinessException("Пользователь заблокирован");
        return SessionUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .avatarPath(user.getAvatarPath())
                .build();
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
