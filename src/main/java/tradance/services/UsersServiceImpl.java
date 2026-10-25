package tradance.services;

import tradance.repository.UsersRepository;

public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;


    public UsersServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
}
