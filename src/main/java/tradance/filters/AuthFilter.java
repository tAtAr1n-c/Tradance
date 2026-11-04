package tradance.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import tradance.dto.ApiResponse;
import tradance.dto.SessionUser;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter({"/wallets", "/orders", "/orders/*", "/trades", "/profile",
        "/api/favorites", "/api/orders", "/api/orders/*", "/profile/*"})
public class AuthFilter implements Filter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if (user == null) {
            if (req.getServletPath().startsWith("/api/")) {
                resp.setStatus(401);
                resp.setContentType("application/json;charset=UTF-8");
                ApiResponse body = ApiResponse.builder().ok(false).error("Нужен вход").build();
                objectMapper.writeValue(resp.getWriter(), body);
            } else {
                if (req.getMethod().equals("GET")) {
                    String target = req.getRequestURI();
                    if (req.getQueryString() != null) {
                        target += "?" + req.getQueryString();
                    }
                    req.getSession().setAttribute("redirectAfterLogin", target);
                }
                resp.sendRedirect(req.getContextPath() + "/login");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

}
