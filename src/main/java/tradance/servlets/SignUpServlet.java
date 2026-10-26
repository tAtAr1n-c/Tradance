package tradance.servlets;


import tradance.dto.SignUpForm;
import tradance.exceptions.ValidationException;
import tradance.services.AuthService;
import tradance.services.AuthServiceImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {


    private AuthService authService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/jsp/signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SignUpForm signUpForm = SignUpForm.builder()
                .username(req.getParameter("username"))
                .email(req.getParameter("email"))
                .password(req.getParameter("password"))
                .passwordRepeat(req.getParameter("passwordRepeat"))
                .build();


        try{
            List<String> words = authService.signUp(signUpForm);
            HttpSession session = req.getSession();
            session.setAttribute("phraseWords", words);
            resp.sendRedirect(req.getContextPath() + "/signup/phrase");
        }catch(ValidationException e){
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("form", signUpForm);
            req.getRequestDispatcher("/jsp/signup.jsp").forward(req, resp);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.authService = (AuthService) config.getServletContext().getAttribute("authService");
    }
}
