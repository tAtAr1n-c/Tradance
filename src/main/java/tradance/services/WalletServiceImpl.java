package tradance.services;

import lombok.*;
import tradance.dto.WalletView;
import tradance.models.Currency;
import tradance.models.Wallet;
import tradance.repository.CurrenciesRepository;
import tradance.repository.WalletsRepository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WalletServiceImpl implements WalletService {

    private final DataSource dataSource;
    private final WalletsRepository walletsRepository;
    private final CurrenciesRepository currenciesRepository;


    public WalletServiceImpl(DataSource dataSource, WalletsRepository walletsRepository, CurrenciesRepository currenciesRepository) {
        this.dataSource = dataSource;
        this.walletsRepository = walletsRepository;
        this.currenciesRepository = currenciesRepository;
    }

    @Override
    public List<WalletView> getWallets(Long userId) {
        List<WalletView> walletViews= new ArrayList<>();
        if(userId == null) return walletViews;
        List<Wallet> wallets = walletsRepository.findAllByUser(userId);
        for(Wallet wallet : wallets){
            Currency currency = currenciesRepository.findById(wallet.getCurrencyId()).orElseThrow(() -> new IllegalStateException("Монета не найдена: " + wallet.getCurrencyId()));
            walletViews.add(WalletView.builder()
                    .ticker(currency.getTicker())
                    .name(currency.getName())
                    .iconPath(currency.getIconPath())
                    .balance(wallet.getBalance())
                    .locked(wallet.getLocked())
                    .available(wallet.getAvailable())
                    .build());
        }

        return walletViews;
    }
}
