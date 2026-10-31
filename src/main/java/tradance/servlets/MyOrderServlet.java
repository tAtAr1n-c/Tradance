package tradance.servlets;

import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.services.OrdersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet({"/orders", "/orders/cancel"})
public class MyOrderServlet extends HttpServlet {
    private OrdersService ordersService;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null){
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.setAttribute("orders", ordersService.findMyOrders(user.getId()));

        req.getRequestDispatcher("/jsp/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null){
            resp.sendError(403);
            return;
        }

        try{
            Long orderId = Long.parseLong(req.getParameter("id"));
            ordersService.cancel(user.getId(), orderId);
            session.setAttribute("flash", "Заказ успешно отменен");
        }catch(NumberFormatException e){
            session.setAttribute("flash" , "Заказ не найдена");
        }catch(BusinessException e){
            session.setAttribute("flash", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ordersService =(OrdersService) config.getServletContext().getAttribute("ordersService");
    }
}
