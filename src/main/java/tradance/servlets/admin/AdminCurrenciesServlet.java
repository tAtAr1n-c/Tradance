package tradance.servlets.admin;


import tradance.dto.CurrencyForm;
import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.models.Currency;
import tradance.models.Role;
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

@WebServlet({"/admin/currencies", "/admin/currencies/new", "/admin/currencies/edit", "/admin/currencies/delete", "/admin/currencies/toggle"})
public class AdminCurrenciesServlet extends HttpServlet {

    private CurrenciesService currenciesService;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null || user.getRole() != Role.ADMIN) {
            resp.sendError(403);
            return;
        }


        String path = req.getServletPath();
        if (path.equals("/admin/currencies/new")) {
            CurrencyForm form = CurrencyForm.builder()
                    .ticker(req.getParameter("ticker"))
                    .name(req.getParameter("name"))
                    .active(req.getParameter("active"))
                    .build();
            try {
                currenciesService.create(form);
                req.getSession().setAttribute("flash", "Монета добавлена");
                resp.sendRedirect(req.getContextPath() + "/admin/currencies");
            } catch (ValidationException e) {
                req.setAttribute("errors", e.getErrors());
                req.setAttribute("form", form);
                List<Currency> currencies = currenciesService.findAll();
                int tradable = 0;
                for (Currency currency : currencies) {
                    if (currency.isActive() && !currency.isQuote()) tradable++;
                }
                req.setAttribute("currencies", currencies);
                req.setAttribute("total", currencies.size());
                req.setAttribute("tradable", tradable);
                req.getRequestDispatcher("/jsp/admin/currencies.jsp").forward(req, resp);
            }
            return;
        }else if(path.equals("/admin/currencies/edit")){
            CurrencyForm form = CurrencyForm.builder()
                    .ticker(req.getParameter("ticker"))
                    .name(req.getParameter("name"))
                    .active(req.getParameter("active"))
                    .build();
            try{
                currenciesService.update(Long.valueOf(req.getParameter("id")), form);
                req.getSession().setAttribute("flash", "Монета изменена");
                resp.sendRedirect(req.getContextPath() + "/admin/currencies");
            }catch(ValidationException e){
                req.setAttribute("currency", currenciesService.findById(Long.valueOf(req.getParameter("id"))));
                req.setAttribute("errors", e.getErrors());
                req.setAttribute("form", form);
                req.getRequestDispatcher("/jsp/admin/currency_edit.jsp").forward(req, resp);
            } catch (NumberFormatException | BusinessException e) {
                resp.sendError(404);
            }
            return;
        }else if(path.equals("/admin/currencies/delete")){
            try {
                currenciesService.delete(Long.valueOf(req.getParameter("id")));
                req.getSession().setAttribute("flash", "Монета удалена");
                resp.sendRedirect(req.getContextPath() + "/admin/currencies");
            }catch(BusinessException e) {
                req.getSession().setAttribute("flash", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/admin/currencies");
            }catch (NumberFormatException e) {
                resp.sendError(404);
            }
        }else if(path.equals("/admin/currencies/toggle")){
            try{
                currenciesService.setActive(Long.valueOf(req.getParameter("id")), "true".equals(req.getParameter("active")));
                req.getSession().setAttribute("flash", "true".equals(req.getParameter("active")) ? "Монета включена" : "Монета выключена");
                resp.sendRedirect(req.getContextPath() + "/admin/currencies");
            }catch(BusinessException e){
                req.getSession().setAttribute("flash", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/admin/currencies");
            }catch(NumberFormatException e){
                resp.sendError(404);
            }
        }
        else{
            resp.sendError(404);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null || user.getRole() != Role.ADMIN) {
            resp.sendError(403);
            return;
        }

        String path = req.getServletPath();

        if(path.equals("/admin/currencies")){

            List<Currency> currencies = currenciesService.findAll();
            int tradable = 0;
            for (Currency currency : currencies) {
                if (currency.isActive() && !currency.isQuote()) tradable++;
            }
            req.setAttribute("currencies", currencies);
            req.setAttribute("total", currencies.size());
            req.setAttribute("tradable", tradable);

            req.getRequestDispatcher("/jsp/admin/currencies.jsp").forward(req, resp);
        }else if(path.equals("/admin/currencies/new")){
            resp.sendRedirect(req.getContextPath() + "/admin/currencies");
        }else if(path.equals("/admin/currencies/edit")){
            try{
                Long id = Long.valueOf(req.getParameter("id"));
                req.setAttribute("currency", currenciesService.findById(id));
                req.getRequestDispatcher("/jsp/admin/currency_edit.jsp").forward(req, resp);
            }catch(NumberFormatException | BusinessException e){
                resp.sendError(404);
            }
        }else{
            resp.sendError(404);
        }
    }


    @Override
    public void init(ServletConfig config) {
        this.currenciesService = (CurrenciesService) config.getServletContext().getAttribute("currenciesService");
    }
}
