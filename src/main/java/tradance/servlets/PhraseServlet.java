package tradance.servlets;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/signup/phrase")
public class PhraseServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if(session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<String> words = (List<String>) session.getAttribute("phraseWords");
        if(words == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        session.removeAttribute("phraseWords");
        req.setAttribute("words", words);
        resp.setHeader("Cache-Control", "no-store");
        req.getRequestDispatcher("/jsp/phrase.jsp").forward(req, resp);
    }


}
