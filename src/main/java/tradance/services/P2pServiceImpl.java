package tradance.services;


import lombok.Synchronized;
import tradance.dto.P2pOfferForm;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.models.*;
import tradance.models.Currency;
import tradance.repository.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class P2pServiceImpl implements P2pService{

    private final DataSource dataSource;
    private final P2pDealsRepository p2pDealsRepository;
    private final P2pOffersRepository p2pOffersRepository;
    private final PaymentMethodsRepository paymentMethodsRepository;
    private final WalletsRepository walletsRepository;
    private final CurrenciesRepository currenciesRepository;


    public P2pServiceImpl(DataSource dataSource, P2pDealsRepository p2pDealsRepository, P2pOffersRepository p2pOffersRepository, PaymentMethodsRepository paymentMethodsRepository, WalletsRepository walletsRepository, CurrenciesRepository currenciesRepository) {
        this.dataSource = dataSource;
        this.p2pDealsRepository = p2pDealsRepository;
        this.p2pOffersRepository = p2pOffersRepository;
        this.paymentMethodsRepository = paymentMethodsRepository;
        this.walletsRepository = walletsRepository;
        this.currenciesRepository = currenciesRepository;
    }

    @Synchronized
    @Override
    public void createOffer(Long userId, P2pOfferForm form) {
        Map<String, String> errors = new HashMap<>();
        if(form.getCurrency() == null){
            throw new BusinessException("Такой монеты нет");
        }
        Optional<Currency> currency = currenciesRepository.findByTicker(form.getCurrency().trim().toUpperCase());
        if(currency.isEmpty() || !currency.get().isActive()){
            errors.put("currency", "Монета недоступна");
        }
        Long paymentMethodId = null;
        try{
            paymentMethodId = Long.valueOf(form.getPaymentMethodId());
            Optional<PaymentMethod> paymentMethod = paymentMethodsRepository.findById(paymentMethodId);
            if(paymentMethod.isEmpty()){
                errors.put("paymentMethodId", "Нет такого способа оплаты");
            }
        }catch(NumberFormatException | NullPointerException e){
            errors.put("paymentMethodId", "Нет такого способа оплаты");
        }

        BigDecimal price = null;
        try{
            price = new BigDecimal(form.getPrice().trim());
            if(price.compareTo(BigDecimal.ZERO) <= 0) errors.put("price", "Цена должна быть больше 0");
            else if(price.scale() > 2) errors.put("price", "У цены не больше 2 знаков после запятой");
        }catch(NumberFormatException | NullPointerException e){
            errors.put("price", "Введите число");
        }
        BigDecimal amount = null;
        try{
            amount = new BigDecimal(form.getAmount().trim());
            if(amount.compareTo(BigDecimal.ZERO) <= 0) errors.put("amount", "Количество должно быть больше 0");
            else if(amount.scale() > 8) errors.put("amount", "Количество знаков после запятой не больше 8");
        }catch(NumberFormatException | NullPointerException e){
            errors.put("amount", "Введите число");
        }
        if(!errors.isEmpty()) throw new ValidationException(errors);
        try(Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try{
                Wallet wallet = walletsRepository.findOrCreate(connection, userId, currency.get().getId());
                BigDecimal remaining = null;
                if(!walletsRepository.lockFunds(connection, wallet.getId(), amount)){
                       throw new BusinessException("Недостаточно средств");
                   }else{
                       remaining = amount;
                   }

                P2pOffer p2pOffer = P2pOffer.builder()
                        .userId(userId)
                        .currencyId(currency.get().getId())
                        .paymentMethodId(paymentMethodId)
                        .price(price)
                        .amount(amount)
                        .remaining(remaining)
                        .status(OfferStatus.ACTIVE)
                        .build();
                p2pOffersRepository.save(connection, p2pOffer);
                connection.commit();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }
            finally{
                connection.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }

    @Synchronized
    @Override
    public void closeOffer(Long userId, Long offerId) {
        if(userId == null) throw new BusinessException("Id у пользователя пустой");
        if(offerId == null) throw new BusinessException("Id у заявки пустой");
        try(Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            try{
                P2pOffer p2pOffer = p2pOffersRepository.findById(connection, offerId).orElseThrow(() -> new BusinessException("Объявление не найдено"));
                if(!p2pOffer.getUserId().equals(userId)) throw new BusinessException("Это не ваше объявление");
                if(p2pOffer.getStatus() != OfferStatus.ACTIVE) throw new BusinessException("Объявление уже закрыто");
                Wallet wallet = walletsRepository.findOrCreate(connection, userId, p2pOffer.getCurrencyId());
                walletsRepository.unlock(connection, wallet.getId(), p2pOffer.getRemaining());
                p2pOffersRepository.updateRemaining(connection, offerId, BigDecimal.ZERO);
                p2pOffersRepository.updateStatus(connection, offerId, OfferStatus.CLOSED);
                connection.commit();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }finally{
                connection.setAutoCommit(true);
            }

            }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }

    @Synchronized
    @Override
    public Long openDeal(Long buyerId, Long offerId, String amountStr) {
        BigDecimal amount = null;
        Map<String, String> errors = new HashMap<>();
        try{
            amount = new BigDecimal(amountStr.trim());
            if(amount.compareTo(BigDecimal.ZERO) <= 0) errors.put("amount","Количество должно быть больше 0");
            else if(amount.scale() > 8) errors.put("amount", "Количество знаков после запятой не больше 8");
        }catch(NumberFormatException | NullPointerException e){
            errors.put("amount", "Введите число");
        }
        if(!errors.isEmpty()) throw new ValidationException(errors);

        try(Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            try{
                P2pOffer p2pOffer = p2pOffersRepository.findById(connection, offerId).orElseThrow(() -> new BusinessException("Объявление не найдено"));
                if(p2pOffer.getStatus() == OfferStatus.CLOSED) throw new BusinessException("Объявление закрыто!");
                if(p2pOffer.getUserId().equals(buyerId)) throw new BusinessException("Нельзя купить у самого себя");
                if(amount.compareTo(p2pOffer.getRemaining()) > 0) throw new BusinessException("Столько нет, доступно: " + p2pOffer.getRemaining());
                p2pOffersRepository.updateRemaining(connection, offerId, p2pOffer.getRemaining().subtract(amount));
                P2pDeal deal = P2pDeal.builder()
                        .offerId(offerId)
                        .buyerId(buyerId)
                        .sellerId(p2pOffer.getUserId())
                        .currencyId(p2pOffer.getCurrencyId())
                        .price(p2pOffer.getPrice())
                        .amount(amount)
                        .status(DealStatus.CREATED)
                        .build();

                p2pDealsRepository.save(connection, deal);


                connection.commit();
                return deal.getId();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }finally{
                connection.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе данных", e);
        }
    }

    @Synchronized
    @Override
    public void markPaid(Long userId, Long dealId) {
        if(userId == null) throw new BusinessException("Не удалось найти пользователя для поиска");
        try(Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            try{
                P2pDeal deal = p2pDealsRepository.findById(connection, dealId).orElseThrow(() -> new BusinessException("Сделка не найдена"));
                if(!deal.getBuyerId().equals(userId)) throw new BusinessException("Это не ваша сделка");
                if(deal.getStatus() != DealStatus.CREATED) throw new BusinessException("Сделку нельзя отметить оплаченной");
                p2pDealsRepository.markPaid(connection, dealId);
                connection.commit();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }finally{
                connection.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе данных", e);
        }
    }

    @Synchronized
    @Override
    public void confirm(Long userId, Long dealId) {
        if(userId == null) throw new IllegalStateException("Не удалось найти пользователя");
        try(Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            try{
                P2pDeal deal = p2pDealsRepository.findById(connection, dealId).orElseThrow(() -> new BusinessException("Сделка не найдена"));
                if(!deal.getSellerId().equals(userId)) throw new BusinessException("Это не ваша сделка");
                if(deal.getStatus() != DealStatus.PAID) throw new BusinessException("Покупатель еще не оплатил");
                Wallet buyerWallet = walletsRepository.findOrCreate(connection, deal.getBuyerId(), deal.getCurrencyId());
                Wallet sellerWallet = walletsRepository.findOrCreate(connection, deal.getSellerId(), deal.getCurrencyId());
                walletsRepository.spendLocked(connection, sellerWallet.getId(), deal.getAmount(), deal.getAmount());
                walletsRepository.credit(connection, buyerWallet.getId(), deal.getAmount());
                p2pDealsRepository.close(connection, deal.getId(), DealStatus.COMPLETED, null);
                connection.commit();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }finally{
                connection.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }
    @Synchronized
    @Override
    public void cancel(Long userId, Long dealId) {
        if(userId == null) throw new IllegalStateException("Не удалось найти пользователя для поиска");
        try(Connection connection = dataSource.getConnection()){
            connection.setAutoCommit(false);
            try{
                P2pDeal deal = p2pDealsRepository.findById(connection, dealId).orElseThrow(() -> new BusinessException("Сделка не найдена"));
                if(!(deal.getSellerId().equals(userId) || deal.getBuyerId().equals(userId))) throw new BusinessException("Это не ваша сделка");


                if(deal.getStatus() == DealStatus.PAID){
                    if(!deal.getBuyerId().equals(userId)) throw new BusinessException("Сделку уже нельзя отменить");
                }else if(deal.getStatus() != DealStatus.CREATED && deal.getStatus() != DealStatus.PAID){
                   throw new BusinessException("Сделку нельзя отменить");

                }P2pOffer offer = p2pOffersRepository.findById(connection, deal.getOfferId()).orElseThrow(() -> new BusinessException("Предложение не найдено"));

                if(offer.getStatus() == OfferStatus.ACTIVE){
                    p2pOffersRepository.updateRemaining(connection , offer.getId(), offer.getRemaining().add(deal.getAmount()));
                }else if(offer.getStatus() == OfferStatus.CLOSED) {
                    Wallet sellerWallet = walletsRepository.findOrCreate(connection, deal.getSellerId(), deal.getCurrencyId());
                    walletsRepository.unlock(connection, sellerWallet.getId(), deal.getAmount());
                }
                p2pDealsRepository.close(connection, dealId, DealStatus.CANCELLED, userId);

                connection.commit();
            }catch(SQLException | RuntimeException e){
                connection.rollback();
                throw e;
            }finally{
                connection.setAutoCommit(true);
            }
        }catch(SQLException e){
            throw new IllegalStateException("Не удалось подключиться к базе", e);
        }
    }

    @Override
    public P2pDeal getDeal(Long userId, Long dealId) {
        P2pDeal p2pDeal = p2pDealsRepository.findById(dealId).orElseThrow(() -> new BusinessException("Сделка не найдена"));
        if(!(p2pDeal.getBuyerId().equals(userId)|| p2pDeal.getSellerId().equals(userId))) throw new BusinessException("Это не ваша сделка");
        return p2pDeal;
    }

    @Override
    public List<P2pOffer> getAllOffers(String ticker) {
        if(ticker == null || ticker.isBlank()) throw new BusinessException("Монета не найдена");
        Currency currency = currenciesRepository.findByTicker(ticker.trim().toUpperCase()).orElseThrow(() -> new BusinessException("Монета не найдена"));
        return p2pOffersRepository.findActive(currency.getId());
    }

    @Override
    public List<P2pOffer> getUserOffers(Long userId) {
        if(userId == null) throw new BusinessException("Пользователь и его предложения не найдены");
        return p2pOffersRepository.findAllByUser(userId);
    }

    @Override
    public List<P2pDeal> getUserDeals(Long userId) {
        if(userId == null) throw new BusinessException("Пользователь и его сделки не найдены");
        return p2pDealsRepository.findAllByUserId(userId);
    }

    @Override
    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethodsRepository.findAll();
    }

    @Override
    public List<Currency> getAllCurrency() {
        return currenciesRepository.findAll().stream().filter(cur -> cur.isActive()).toList();
    }
}
