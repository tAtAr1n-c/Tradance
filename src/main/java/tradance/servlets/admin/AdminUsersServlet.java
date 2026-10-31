package tradance.servlets.admin;


import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.services.CurrenciesService;
import tradance.services.UsersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet({"/admin/users", "/admin/users/block", "/admin/users/deposit"})
public class AdminUsersServlet extends HttpServlet {
    private UsersService usersService;
    private CurrenciesService currenciesService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", usersService.findAll());
        req.setAttribute("currencies", currenciesService.findAll());

        req.getRequestDispatcher("/jsp/admin/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser admin = (session == null) ? null : (SessionUser) session.getAttribute("user");
        String path = req.getServletPath();

        if (path.equals("/admin/users/block")) {
            try {
                Long id = Long.parseLong(req.getParameter("id"));
                boolean blocked = "true".equals(req.getParameter("blocked"));
                if (id.equals(admin.getId())) {
                    session.setAttribute("flash", "Нельзя заблокировать себя");
                } else {
                    usersService.setBlocked(id, blocked);
                    if (blocked) {
                        session.setAttribute("flash", "Пользователь заблокирован");
                    } else {
                        session.setAttribute("flash", "Пользователь разблокирован");
                    }
                }
            } catch (NumberFormatException e) {
                session.setAttribute("flash", "Пользователь не найден");
            } catch (BusinessException e) {
                session.setAttribute("flash", e.getMessage());
            }
        } else if (path.equals("/admin/users/deposit")) {
            try {
                usersService.deposit(Long.parseLong(req.getParameter("userId")), req.getParameter("currency"), req.getParameter("amount"));
                session.setAttribute("flash", "Баланс пополнен");
            } catch (ValidationException e) {
                session.setAttribute("flash", String.join(", ", e.getErrors().values()));
            } catch (NumberFormatException e) {
                session.setAttribute("flash", "Пользователь не найден");
            } catch (BusinessException e) {
                session.setAttribute("flash", e.getMessage());
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.usersService = (UsersService) config.getServletContext().getAttribute("usersService");
        this.currenciesService = (CurrenciesService) config.getServletContext().getAttribute("currenciesService");
    }
}
