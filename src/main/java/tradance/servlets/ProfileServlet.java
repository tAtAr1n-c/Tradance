package tradance.servlets;


import tradance.dto.SessionUser;
import tradance.exceptions.BusinessException;
import tradance.services.FileStorageService;
import tradance.services.UsersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;

@WebServlet({"/profile", "/profile/avatar"})
@MultipartConfig
public class ProfileServlet extends HttpServlet {

    private UsersService usersService;
    private FileStorageService fileStorageService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        if(user == null){
            resp.sendError(403);
            return;
        }
        String path = req.getServletPath();

        if(path.equals("/profile")){
            try{
                req.setAttribute("profile", usersService.findById(user.getId()));
                req.getRequestDispatcher("/jsp/profile.jsp").forward(req, resp);
            }catch(BusinessException e){
                resp.sendError(404);
            }
        }else{
            resp.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        SessionUser user = (session == null) ? null : (SessionUser) session.getAttribute("user");

        if(user == null){
            resp.sendError(403);
            return;
        }
        String path = req.getServletPath();

        if(path.equals("/profile/avatar")){
            try{
                Part part = req.getPart("avatar");
                Long fileId = fileStorageService.save(part, user.getId());
                usersService.setAvatar(user.getId(), fileId);
                user.setAvatarFileId(fileId);
                session.setAttribute("user", user);
                session.setAttribute("flash", "Аватар обновлён");
            }catch(BusinessException e){
                session.setAttribute("flashError", e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/profile");
        }else{
            resp.sendError(404);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.usersService = (UsersService) config.getServletContext().getAttribute("usersService");
        this.fileStorageService = (FileStorageService) config.getServletContext().getAttribute("fileStorageService");
    }
}
