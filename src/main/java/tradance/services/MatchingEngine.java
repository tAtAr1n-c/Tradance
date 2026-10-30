package tradance.services;

import tradance.models.Order;

import java.sql.Connection;

public interface MatchingEngine {
    void match(Connection con, Order newOrder);

}
