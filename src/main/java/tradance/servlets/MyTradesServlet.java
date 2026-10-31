package tradance.servlets;


import tradance.dto.SessionUser;
import tradance.services.OrdersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/trades")
public class MyTradesServlet extends HttpServlet {


    private OrdersService ordersService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("trades", ordersService.findMyTrades(user.getId()));

        req.getRequestDispatcher("/jsp/trades.jsp").forward(req, resp);

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ordersService = (OrdersService) config.getServletContext().getAttribute("ordersService");
    }
}
