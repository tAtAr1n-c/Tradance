package tradance.servlets;


import tradance.dto.SessionUser;
import tradance.dto.WalletView;
import tradance.services.WalletService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/wallets")
public class WalletsServlet extends HttpServlet {
    private WalletService walletService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (SessionUser) session.getAttribute("user");
        List<WalletView> wallets =  walletService.getWallets(user.getId());
        req.setAttribute("wallets", wallets);

        for(WalletView wallet : wallets){
            if("USDT".equals(wallet.getTicker())){
                req.setAttribute("usdt", wallet);
                break;
            }
        }
        req.getRequestDispatcher("/jsp/wallets.jsp").forward(req, resp);



    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.walletService = (WalletService) config.getServletContext().getAttribute("walletService");
    }
}
