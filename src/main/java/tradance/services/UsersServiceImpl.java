package tradance.services;

import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.models.Currency;
import tradance.models.User;
import tradance.models.Wallet;
import tradance.repository.CurrenciesRepository;
import tradance.repository.UsersRepository;
import tradance.repository.WalletsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersServiceImpl implements UsersService{

    private final DataSource dataSource;
    private final UsersRepository usersRepository;
    private final WalletsRepository walletsRepository;
    private final CurrenciesRepository currenciesRepository;

    public UsersServiceImpl(DataSource dataSource, UsersRepository usersRepository, WalletsRepository walletsRepository, CurrenciesRepository currenciesRepository) {
        this.dataSource = dataSource;
        this.usersRepository = usersRepository;
        this.walletsRepository = walletsRepository;
        this.currenciesRepository = currenciesRepository;
    }

    @Override
    public List<User> findAll() {

        return usersRepository.findAll();
    }

    @Override
    public void setBlocked(Long id, boolean blocked) {
        if(id == null) {
            throw new BusinessException("Пользователь не найден");
        }
        User user = usersRepository.findById(id).orElseThrow(() -> new BusinessException("Пользователь не найден"));
        usersRepository.updateBlocked(id, blocked);

    }

    @Override
    public void deposit(Long userId, String currencyTicker, String amount) {
        if(userId == null) {
            throw new BusinessException("Пользователь не найден");
        }
        Map<String, String> errors = new HashMap<>();
        usersRepository.findById(userId).orElseThrow(() -> new BusinessException("Пользователь не найден"));
        Currency currency = null;
        if(currencyTicker == null || currencyTicker.isBlank()) errors.put("currency", "Выберите монету");
        else {
            currency = currenciesRepository.findByTicker(currencyTicker.trim().toUpperCase()).orElse(null);
        }
        if (currency == null) errors.put("currency", "Монеты с таким тикером нет");
        BigDecimal value = null;
        try{
            value = new BigDecimal(amount.trim());
            if(value.compareTo(BigDecimal.ZERO) <= 0) errors.put("amount", "Количество должно быть больше нуля");
            else if(value.scale() > 8) errors.put("amount", "Не больше 8 знаков после запятой");
        }catch(NumberFormatException | NullPointerException e){
           errors.put("amount", "Введите число");
        }
        if(!errors.isEmpty()) throw new ValidationException(errors);
        try(Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            try{
                Wallet wallet = walletsRepository.findOrCreate(connection, userId, currency.getId());
                walletsRepository.credit(connection, wallet.getId(), value);
                connection.commit();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }finally{
                connection.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }
}
