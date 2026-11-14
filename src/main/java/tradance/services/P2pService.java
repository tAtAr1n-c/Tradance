package tradance.services;


import tradance.dto.P2pOfferForm;
import tradance.models.Currency;
import tradance.models.P2pDeal;
import tradance.models.P2pOffer;
import tradance.models.PaymentMethod;


import java.util.List;



public interface P2pService {
    void createOffer(Long userId, P2pOfferForm form);
    void closeOffer(Long userId, Long offerId);
    Long openDeal(Long buyerId, Long offerId, String amount);
    void markPaid(Long userId, Long dealId);
    void confirm(Long userId, Long dealId);
    void cancel(Long userId, Long dealId);
    P2pDeal getDeal(Long userId, Long dealId);
    List<P2pOffer> getAllOffers(String ticker);
    List<P2pOffer> getUserOffers(Long userId);
    List<P2pDeal> getUserDeals(Long userId);
    List<PaymentMethod> getPaymentMethods();
    List<Currency> getAllCurrency();
}
