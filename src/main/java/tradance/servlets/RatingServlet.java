package tradance.servlets;


import tradance.dto.RatingRow;
import tradance.services.RatingService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/rating")
public class RatingServlet extends HttpServlet {

    private RatingService ratingService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<RatingRow> rows = ratingService.getRating();
        req.setAttribute("rows", rows);
        req.getRequestDispatcher("/jsp/rating.jsp").forward(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ratingService = (RatingService) config.getServletContext().getAttribute("ratingService");
    }
}
