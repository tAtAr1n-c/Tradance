package tradance.services;

import tradance.models.Deposit;
import tradance.models.User;

import java.util.List;

public interface UsersService {
    List<User> findAll();
    void setBlocked(Long id, boolean blocked);
    void deposit(Long userId,Long adminId, String currencyTicker, String amount);
    User findById(Long id);
    void setAvatar(Long userId, Long fileId);
    List<Deposit> findAllDeposits();
}
