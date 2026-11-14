package tradance.repository;

import tradance.models.OfferStatus;
import tradance.models.P2pOffer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface P2pOffersRepository {
    void save(Connection connection, P2pOffer p2pOffer);
    Optional<P2pOffer> findById(Connection connection, Long id);
    List<P2pOffer> findActive(Long currencyId);
    List<P2pOffer> findAllByUser(Long userId);
    void updateRemaining(Connection connection, Long id, BigDecimal remaining);
    void updateStatus(Connection connection, Long id, OfferStatus offerStatus);
}

