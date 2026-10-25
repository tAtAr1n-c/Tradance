package tradance.services;

import tradance.repository.CurrenciesRepository;
import tradance.repository.TradesRepository;

public class CurrenciesServiceImpl implements CurrenciesService {

    CurrenciesRepository currenciesRepository;
    TradesRepository tradesRepository;
    FileStorageService fileStorageService;

    public CurrenciesServiceImpl(CurrenciesRepository currenciesRepository, TradesRepository tradesRepository, FileStorageService fileStorageService) {
        this.currenciesRepository = currenciesRepository;
        this.tradesRepository = tradesRepository;
        this.fileStorageService = fileStorageService;
    }
}
