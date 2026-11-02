package tradance.servlets;


import tradance.dto.SessionUser;
import tradance.services.CurrenciesService;
import tradance.services.OrdersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/trades")
public class MyTradesServlet extends HttpServlet {


    private OrdersService ordersService;
    private CurrenciesService currenciesService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("trades", ordersService.findMyTrades(user.getId()));
        Map<Long, String> tickers = currenciesService.findAll().stream().collect(Collectors.toMap(item -> item.getId(), item -> item.getTicker()));
        req.setAttribute("tickers", tickers);

        req.getRequestDispatcher("/jsp/trades.jsp").forward(req, resp);

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ordersService = (OrdersService) config.getServletContext().getAttribute("ordersService");
        this.currenciesService = (CurrenciesService) config.getServletContext().getAttribute("currenciesService");
    }
}
