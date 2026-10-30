package tradance.servlets;


import tradance.dto.OrderBookDto;
import tradance.dto.OrderForm;
import tradance.dto.SessionUser;
import tradance.dto.WalletView;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.services.OrdersService;
import tradance.services.WalletService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;


@WebServlet("/trade")
public class TradeServlet extends HttpServlet {

    private OrdersService ordersService;
    private WalletService walletService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        String ticker = req.getParameter("currency");
        if(ticker == null || ticker.isBlank()){
            resp.sendRedirect(req.getContextPath() + "/market");
            return;
        }

        OrderBookDto book;
        try{
            book = ordersService.getOrderBook(ticker);
        }catch(BusinessException e){
            req.setAttribute("errorMessage", "Пары " + ticker + "/USDT нет");
            resp.sendError(404);
            return;
        }
        req.setAttribute("book", book);
        req.setAttribute("currency", book.getCurrency());
        if(user != null) {
            BigDecimal usdtAvailable = BigDecimal.ZERO;
            BigDecimal coinAvailable = BigDecimal.ZERO;
            for(WalletView wallet : walletService.getWallets(user.getId())){
                if ("USDT".equals(wallet.getTicker())) usdtAvailable = wallet.getAvailable();
                if(book.getCurrency().getTicker().equals(wallet.getTicker())){
                    coinAvailable = wallet.getAvailable();
                }
            }
            req.setAttribute("usdtAvailable", usdtAvailable);
            req.setAttribute("coinAvailable", coinAvailable);
        }

        req.getRequestDispatcher("/jsp/trade.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null){
            resp.sendError(403);
            return;
        }
                OrderForm orderForm = OrderForm.builder()
                .currency(req.getParameter("currency"))
                .side(req.getParameter("side"))
                .price(req.getParameter("price"))
                .amount(req.getParameter("amount"))
                .build();
        String ticker = (orderForm.getCurrency() == null) ? "" : orderForm.getCurrency().trim().toUpperCase();
        try{
            ordersService.place(user.getId(), orderForm);
            session.setAttribute("flash", "Заявка создана");
            resp.sendRedirect(req.getContextPath() + "/trade?currency=" + ticker);
        }catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("form", orderForm);
            doGet(req, resp);
        } catch (BusinessException e) {
            session.setAttribute("flash", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/trade?currency=" + ticker);
        }

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ordersService = (OrdersService) config.getServletContext().getAttribute("ordersService");
        this.walletService = (WalletService)  config.getServletContext().getAttribute("walletService");
    }
}
