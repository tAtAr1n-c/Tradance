package tradance.servlets.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tradance.dto.ApiResponse;
import tradance.dto.OrderBookDto;
import tradance.exceptions.BusinessException;
import tradance.services.OrdersService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/orderbook")
public class OrderBookApiServlet extends HttpServlet {
    private OrdersService ordersService;
    private ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        ApiResponse response;
        try{
            OrderBookDto book = ordersService.getOrderBook(req.getParameter("currency"));
            response = ApiResponse.builder().ok(true).data(book).build();
        }catch(BusinessException e){
            resp.setStatus(404);
            response = ApiResponse.builder().ok(false).error(e.getMessage()).build();
        }
        objectMapper.writeValue(resp.getWriter(), response);



    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.ordersService = (OrdersService) config.getServletContext().getAttribute("ordersService");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
