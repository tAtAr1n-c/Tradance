package tradance.services;

import tradance.repository.CurrenciesRepository;
import tradance.repository.OrdersRepository;
import tradance.repository.TradesRepository;

import javax.sql.DataSource;

public class OrdersServiceImpl implements OrdersService {

    private final DataSource dataSource;
    private final OrdersRepository ordersRepository;
    private final CurrenciesRepository currenciesRepository;
    private final TradesRepository tradesRepository;
    private final MatchingEngine matchingEngine;

    public OrdersServiceImpl(DataSource dataSource, OrdersRepository ordersRepository, CurrenciesRepository currenciesRepository, TradesRepository tradesRepository, MatchingEngine matchingEngine) {
        this.dataSource = dataSource;
        this.ordersRepository = ordersRepository;
        this.currenciesRepository = currenciesRepository;
        this.tradesRepository = tradesRepository;
        this.matchingEngine = matchingEngine;
    }
}
