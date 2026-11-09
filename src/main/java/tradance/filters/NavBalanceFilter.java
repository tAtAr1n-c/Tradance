package tradance.filters;

import tradance.dto.SessionUser;
import tradance.dto.WalletView;
import tradance.services.WalletService;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter({"/market", "/trade", "/wallets", "/orders", "/trades", "/profile", "/rating"})
public class NavBalanceFilter implements Filter {

    private WalletService walletService;

    @Override
    public void init(FilterConfig filterConfig) {
        this.walletService = (WalletService) filterConfig.getServletContext().getAttribute("walletService");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        if (user != null && req.getMethod().equals("GET")) {
            for (WalletView wallet : walletService.getWallets(user.getId())) {
                if ("USDT".equals(wallet.getTicker())) {
                    req.setAttribute("navUsdtAvailable", wallet.getAvailable());
                }
            }
        }
        chain.doFilter(request, response);
    }
}
