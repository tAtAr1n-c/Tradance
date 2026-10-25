package tradance.services;

import tradance.repository.CurrenciesRepository;
import tradance.repository.WalletsRepository;

import javax.sql.DataSource;

public class WalletServiceImpl implements WalletService {

    private final DataSource dataSource;
    private final WalletsRepository walletsRepository;
    private final CurrenciesRepository currenciesRepository;


    public WalletServiceImpl(DataSource dataSource, WalletsRepository walletsRepository, CurrenciesRepository currenciesRepository) {
        this.dataSource = dataSource;
        this.walletsRepository = walletsRepository;
        this.currenciesRepository = currenciesRepository;
    }

}
