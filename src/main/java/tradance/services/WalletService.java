package tradance.services;

import tradance.dto.WalletView;

import java.util.List;

public interface WalletService {
    List<WalletView> getWallets(Long userId);

}
