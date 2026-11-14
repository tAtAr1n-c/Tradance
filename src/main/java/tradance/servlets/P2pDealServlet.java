package tradance.servlets;

import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.models.Currency;
import tradance.models.P2pDeal;
import tradance.models.PaymentMethod;
import tradance.models.User;
import tradance.services.P2pService;
import tradance.services.UsersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@WebServlet({"/p2p/deal", "/p2p/deals", "/p2p/deal/open", "/p2p/deal/paid", "/p2p/deal/confirm", "/p2p/deal/cancel"})
public class P2pDealServlet extends HttpServlet {
    private P2pService p2pService;
    private UsersService usersService;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        String path = req.getServletPath();
        if(path.equals("/p2p/deal/open")){
            Long offerId = null;
            try{
                offerId = Long.valueOf(req.getParameter("offerId"));
            }catch(NumberFormatException e){
                resp.sendError(404);
                return;
            }
            try{
                Long id = p2pService.openDeal(user.getId(), offerId, req.getParameter("amount"));
                session.setAttribute("flash", "Сделка открыта");
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id="+id);
            }catch(BusinessException e){
                session.setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/p2p");
            }catch(ValidationException e){
                session.setAttribute("flashError", e.getErrors().get("amount"));
                resp.sendRedirect(req.getContextPath() + "/p2p");
            }
        }else if(path.equals("/p2p/deal/paid")){
            Long dealId = null;
            try{
                dealId = Long.valueOf(req.getParameter("id"));
            }catch(NumberFormatException e){
                resp.sendError(404);
                return;
            }
            try{
                p2pService.markPaid(user.getId(), dealId);
                session.setAttribute("flash", "Оплачено");
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id=" + dealId);
            }catch(BusinessException e){
                session.setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id=" + dealId);
            }
        }else if(path.equals("/p2p/deal/confirm")){
            Long dealId = null;
            try{
                dealId = Long.valueOf(req.getParameter("id"));
            }catch(NumberFormatException e){
                resp.sendError(404);
                return;
            }
            try{
                p2pService.confirm(user.getId(), dealId);
                session.setAttribute("flash", "Монеты отправлены покупателю");
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id=" + dealId);
            }catch(BusinessException e){
                session.setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id=" + dealId);
            }
        }else if(path.equals("/p2p/deal/cancel")){
            Long dealId = null;
            try{
                dealId = Long.valueOf(req.getParameter("id"));
            }catch(NumberFormatException e){
                resp.sendError(404);
                return;
            }
            try{
                p2pService.cancel(user.getId(), dealId);
                session.setAttribute("flash", "Сделка отменена");
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id=" + dealId);
            }catch(BusinessException e){
                session.setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/p2p/deal?id=" + dealId);
            }
        }else{
            resp.sendError(404);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        String path = req.getServletPath();

        Map<Long, String> paymentMethods = p2pService.getPaymentMethods().stream().collect(Collectors.toMap(PaymentMethod::getId, PaymentMethod::getName));
        Map<Long, String> currencies = p2pService.getAllCurrency().stream().collect(Collectors.toMap(Currency::getId, Currency::getTicker));
        Map<Long, String> users = usersService.findAll().stream().collect(Collectors.toMap(User::getId, User::getUsername));

        req.setAttribute("paymentMethods", paymentMethods);
        req.setAttribute("currencies", currencies);
        req.setAttribute("usersMap", users);

        if(path.equals("/p2p/deal")){
            Long id = null;
            try{
                id = Long.valueOf(req.getParameter("id"));
            }catch(NumberFormatException e){
                resp.sendError(404);
                return;
            }

            P2pDeal p2pDeal = null;
            try{
                p2pDeal = p2pService.getDeal(user.getId(), id);
            }catch(BusinessException e){
                resp.sendError(404);
                return;
            }
            req.setAttribute("deal", p2pDeal);


            req.getRequestDispatcher("/jsp/p2p_deal.jsp").forward(req, resp);
        }else if(path.equals("/p2p/deals")){
            List<P2pDeal> p2pDeals = new ArrayList<>();
            try{
                p2pDeals = p2pService.getUserDeals(user.getId());
            }catch(BusinessException e){
                resp.sendError(404);
                return;
            }
            req.setAttribute("deals", p2pDeals);
            req.getRequestDispatcher("/jsp/p2p_deals.jsp").forward(req, resp);
        }else{
            resp.sendError(404);
        }

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
       this.p2pService = (P2pService) config.getServletContext().getAttribute("p2pService");
       this.usersService = (UsersService) config.getServletContext().getAttribute("usersService");
    }



}

