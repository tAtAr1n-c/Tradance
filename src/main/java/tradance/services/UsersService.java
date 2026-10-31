package tradance.services;

import tradance.models.User;

import java.util.List;

public interface UsersService {
    List<User> findAll();
    void setBlocked(Long id, boolean blocked);
    void deposit(Long userId, String currencyTicker, String amount);
}
