package tradance.servlets;

import tradance.dto.MarketRow;
import tradance.dto.SessionUser;
import tradance.services.CurrenciesService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/market")
public class MarketServlet extends HttpServlet {

    private CurrenciesService currenciesService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        Long userId = (user == null) ? null : user.getId();
        List<MarketRow> marketRowList = currenciesService.getMarket(userId);
        req.setAttribute("market", marketRowList);
        req.getRequestDispatcher("/jsp/market.jsp").forward(req, resp);
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        this.currenciesService = (CurrenciesService) config.getServletContext().getAttribute("currenciesService");
    }

}
