package tradance.servlets;

import tradance.dto.P2pOfferForm;
import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.models.Currency;
import tradance.models.P2pOffer;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet({"/p2p", "/p2p/offers/new", "/p2p/offers/close"})
public class P2pServlet extends HttpServlet {

    private P2pService p2pService;
    private UsersService usersService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        String path = req.getServletPath();

        if (path.equals("/p2p/offers/new")) {
            P2pOfferForm p2pOfferForm = P2pOfferForm.builder()
                    .currency(req.getParameter("currency"))
                    .paymentMethodId(req.getParameter("paymentMethodId"))
                    .price(req.getParameter("price"))
                    .amount(req.getParameter("amount"))
                    .build();
            try {
                p2pService.createOffer(user.getId(), p2pOfferForm);
                session.setAttribute("flash", "Предложение создано");
                resp.sendRedirect(req.getContextPath() + "/p2p?currency="+ p2pOfferForm.getCurrency());
            }catch (ValidationException e) {
                req.setAttribute("errors", e.getErrors());
                req.setAttribute("form", p2pOfferForm);
                doGet(req, resp);
            } catch (BusinessException e) {
                session.setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/p2p?currency="+p2pOfferForm.getCurrency());
            }
        } else if (path.equals("/p2p/offers/close")) {
            Long id = null;
            try {
                id = Long.parseLong(req.getParameter("id"));
            }catch(NumberFormatException e){
                resp.sendError(404);
                return;
            }
            try{
                p2pService.closeOffer(user.getId(), id);
                session.setAttribute("flash", "Предложение успешно отменено");
                resp.sendRedirect(req.getContextPath() + "/p2p");
            }catch(BusinessException e){
                session.setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/p2p");
            }
        } else {
            resp.sendError(404);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        String ticker = req.getParameter("currency");
        if (ticker == null || ticker.isBlank()) {
            ticker = "BTC";
        }
        ticker = ticker.trim().toUpperCase();
        List<P2pOffer> offers = null;
        try {
            offers = p2pService.getAllOffers(ticker);
        } catch (BusinessException e) {
            resp.sendError(404);
            return;
        }
        Map<Long, String> users = usersService.findAll().stream().collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> paymentMethods = p2pService.getPaymentMethods().stream().collect(Collectors.toMap(PaymentMethod::getId, PaymentMethod::getName));

        List<Currency> currencies = p2pService.getAllCurrency();

        List<P2pOffer> myOffers = null;
        if(user != null){
            myOffers = p2pService.getUserOffers(user.getId());
        }



        req.setAttribute("offers", offers);
        req.setAttribute("usersMap", users);
        req.setAttribute("paymentMethods", paymentMethods);
        req.setAttribute("currencies", currencies);
        req.setAttribute("ticker", ticker);
        req.setAttribute("myOffers", myOffers);

        req.getRequestDispatcher("/jsp/p2p.jsp").forward(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        p2pService = (P2pService) config.getServletContext().getAttribute("p2pService");
        usersService = (UsersService) config.getServletContext().getAttribute("usersService");
    }
}
