package tradance.repository;

import tradance.models.DealStatus;
import tradance.models.P2pDeal;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface P2pDealsRepository {
    void save(Connection connection, P2pDeal p2pDeal);
    Optional<P2pDeal> findById(Connection connection, Long id);
    Optional<P2pDeal> findById(Long id);
    List<P2pDeal> findAllByUserId(Long userId);
    void markPaid(Connection connection, Long id);
    void close(Connection connection, Long id, DealStatus dealStatus, Long cancelledBy);
}
