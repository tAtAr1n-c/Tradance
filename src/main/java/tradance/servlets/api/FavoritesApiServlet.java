package tradance.servlets.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tradance.dto.ApiResponse;
import tradance.dto.FavoriteForm;
import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.services.CurrenciesService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/favorites")
public class FavoritesApiServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper();
    private CurrenciesService currenciesService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        ApiResponse response;
        try {
            FavoriteForm form = objectMapper.readValue(req.getReader(), FavoriteForm.class);
            boolean favorite = currenciesService.toggleFavorite(user.getId(), form.getCurrencyId());
            response = ApiResponse.builder()
                    .ok(true)
                    .data(Map.of("favorite", favorite))
                    .build();
        } catch (JsonProcessingException e) {
            resp.setStatus(400);
            response = ApiResponse.builder().ok(false).error("Неверный запрос").build();
        } catch (BusinessException e) {
            resp.setStatus(400);
            response = ApiResponse.builder().ok(false).error(e.getMessage()).build();
        }

        objectMapper.writeValue(resp.getWriter(), response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.currenciesService = (CurrenciesService) config.getServletContext().getAttribute("currenciesService");
    }
}
