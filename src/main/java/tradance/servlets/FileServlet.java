package tradance.servlets;


import tradance.models.FileInfo;
import tradance.services.FileStorageService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
        Long id;
        try{
            id = Long.parseLong(idTake);
        }catch(NumberFormatException e){
            resp.sendError(404);
            return;
        }

        Optional<FileInfo> fileInfo = fileStorageService.findById(id);
        if(fileInfo.isEmpty()){
            resp.sendError(404);
            return;
        }

        FileInfo info = fileInfo.get();
        Path path = fileStorageService.getPath(info.getStorageName());
        if(!Files.exists(path)){
            resp.sendError(404);
            return;
        }

        resp.setContentType(info.getContentType());
        resp.setContentLengthLong(info.getSize());
        Files.copy(path, resp.getOutputStream());
    }



    @Override
    public void init(ServletConfig config) throws ServletException {
        this.fileStorageService = (FileStorageService) config.getServletContext().getAttribute("fileStorageService");
    }
}
