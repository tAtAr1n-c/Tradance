package tradance.filters;

import tradance.dto.SessionUser;
import tradance.models.Role;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/admin/*")
public class AdminFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");
        if(user == null){
            if (req.getMethod().equals("GET")) {
                String target= req.getRequestURI();
                if(req.getQueryString() != null){
                    target += "?" + req.getQueryString();
                }
                req.getSession().setAttribute("redirectAfterLogin", target);
            }
            resp.sendRedirect(req.getContextPath() + "/login");
        }else if(user.getRole() != Role.ADMIN){
            resp.sendError(404);
        }else{
            chain.doFilter(req, resp);
        }
    }
}
