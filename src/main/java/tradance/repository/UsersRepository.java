package tradance.repository;

import tradance.models.User;

import java.util.Optional;

public interface UsersRepository extends CrudRepository<User> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void updateBlocked(Long id, boolean blocked);

    void updateAvatar(Long id, String path);

    void updatePasswordHash(Long id, String hash);


}
