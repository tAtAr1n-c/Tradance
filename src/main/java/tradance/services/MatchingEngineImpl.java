package tradance.services;

import tradance.repository.OrdersRepository;
import tradance.repository.TradesRepository;
import tradance.repository.WalletsRepository;

public class MatchingEngineImpl implements MatchingEngine {

    private final OrdersRepository ordersRepository;
    private final WalletsRepository walletsRepository;
    private final TradesRepository tradesRepository;


    public MatchingEngineImpl(OrdersRepository ordersRepository, WalletsRepository walletsRepository, TradesRepository tradesRepository) {
        this.ordersRepository = ordersRepository;
        this.walletsRepository = walletsRepository;
        this.tradesRepository = tradesRepository;
    }
}
