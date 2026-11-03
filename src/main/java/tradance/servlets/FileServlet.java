package tradance.servlets;


import tradance.services.FileStorageService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/files")
public class FileServlet extends HttpServlet {

    private FileStorageService fileStorageService;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idTake = req.getParameter("id");
        if(idTake == null){
            resp.sendError(404);
            return;
        }
        try{
            Long id = Long.parseLong(idTake);
        }catch(NumberFormatException e){
            resp.sendError(404);
            return;
        }
    }



    @Override
    public void init(ServletConfig config) throws ServletException {
        this.fileStorageService = (FileStorageService) config.getServletContext().getAttribute("fileStorageService");
    }
}
