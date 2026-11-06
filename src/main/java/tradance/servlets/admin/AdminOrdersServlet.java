package tradance.servlets.admin;

import tradance.models.Currency;
import tradance.models.User;
import tradance.models.Trade;
import tradance.models.Order;
import tradance.services.CurrenciesService;
import tradance.services.OrdersService;
import tradance.services.UsersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet({"/admin/orders", "/admin/trades"})
public class AdminOrdersServlet extends HttpServlet {
    private OrdersService ordersService;
    private CurrenciesService currenciesService;
    private UsersService usersService;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath();
        Map<Long, String> currencyMap = currenciesService.findAll().stream().collect(Collectors.toMap(Currency::getId, Currency::getTicker));
        Map<Long, String> usersMap = usersService.findAll().stream().collect(Collectors.toMap(User::getId, User::getUsername));

        req.setAttribute("currencyMap", currencyMap);
        req.setAttribute("usersMap", usersMap);
        if(path.equals("/admin/orders")){
            List<Order> orders = ordersService.findAll();
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("/jsp/admin/orders.jsp").forward(req, resp);
        }else if(path.equals("/admin/trades")){
            List<Trade> trades = ordersService.findAllTrades();
            req.setAttribute("trades", trades);
            req.getRequestDispatcher("/jsp/admin/trades.jsp").forward(req, resp);
        }else{
            resp.sendError(404);
        }


    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ordersService = (OrdersService) config.getServletContext().getAttribute("ordersService");
        this.usersService = (UsersService) config.getServletContext().getAttribute("usersService");
        this.currenciesService = (CurrenciesService) config.getServletContext().getAttribute("currenciesService");
    }
}
