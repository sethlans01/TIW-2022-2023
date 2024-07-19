package controllers;

import beans.Order;
import beans.User;
import com.google.gson.Gson;
import dao.OrdersDAO;
import utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet("/Orders")
public class Orders extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public Orders() {
        super();
    }

    @Override
    public void init() throws ServletException {
        // Get connection to DB
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        //Call db, fetch orders' data and send them to the client
        Order order = new Order();

        OrdersDAO dao = new OrdersDAO(connection);

        User user = (User) session.getAttribute("currentUser");

        try{
            order.setOrderList(dao.produceOrderList(user.getEmail()));
        } catch (SQLException e){
            order.setOrderList(new ArrayList<>());
        }

        String json = new Gson().toJson(order);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(json);


    }

}