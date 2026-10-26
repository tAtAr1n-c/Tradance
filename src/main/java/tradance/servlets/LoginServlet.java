package tradance.servlets;


import tradance.dto.LoginForm;
import tradance.dto.PendingLogin;
import tradance.exceptions.BusinessException;
import tradance.exceptions.ValidationException;
import tradance.services.AuthService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthService authService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.authService = (AuthService) config.getServletContext().getAttribute("authService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginForm loginForm = LoginForm.builder()
                .username(req.getParameter("username"))
                .password(req.getParameter("password"))
                .build();
        try{
            PendingLogin pendingLogin = authService.startLogin(loginForm);
            HttpSession session = req.getSession();
            session.setAttribute("pendingLogin", pendingLogin);
            resp.sendRedirect(req.getContextPath() + "/login/verify");
        }catch(BusinessException e){
            req.setAttribute("error", e.getMessage());
            req.setAttribute("form", loginForm);
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
    }
}
