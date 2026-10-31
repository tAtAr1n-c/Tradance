package tradance.servlets;


import tradance.dto.PendingLogin;
import tradance.dto.PhraseCheckForm;
import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.services.AuthService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/login/verify")
public class LoginVerifyServlet extends HttpServlet {

    private AuthService authService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        PendingLogin pendingLogin = (PendingLogin) session.getAttribute("pendingLogin");
        if(pendingLogin == null){
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("positions", pendingLogin.getPositions());
        req.getRequestDispatcher("/jsp/login_verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        PendingLogin pending = (session == null) ? null : (PendingLogin) session.getAttribute("pendingLogin");
        if (pending == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String[] words = req.getParameterValues("words");
        PhraseCheckForm form = PhraseCheckForm.builder()
                .words(words == null ? List.of() : Arrays.asList(words))
                .build();

        try {
            SessionUser user = authService.finishLogin(pending.getUserId(), pending.getPositions(), form);

            session.removeAttribute("pendingLogin");
            req.changeSessionId();
            session.setAttribute("user", user);

            resp.sendRedirect((String) session.getAttribute("redirectAfterLogin"));
            session.removeAttribute("redirectAfterLogin");
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("positions", pending.getPositions());
            req.getRequestDispatcher("/jsp/login_verify.jsp").forward(req, resp);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.authService = (AuthService) config.getServletContext().getAttribute("authService");
    }
}
