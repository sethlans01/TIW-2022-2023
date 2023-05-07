package controllers;

import beans.Order;
import beans.User;
import dao.OrdersDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ConnectionHandler;
import utils.PathUtils;

import javax.servlet.ServletContext;
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

import static utils.Statics.checkAccess;
import static utils.TemplateHandler.getEngine;

@WebServlet("/Orders")
public class Orders extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Orders() {
        super();
    }

    @Override
    public void init() throws ServletException {
        // Create Thymeleaf Engine
        templateEngine = getEngine(getServletContext(), ".html");
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
        // Grab current session
        HttpSession session = request.getSession();

        if(checkAccess(session)){

            // Create an Order bean
            Order currentOrderBean = new Order();

            // Create an OrdersDAO object
            OrdersDAO ordersDAO = new OrdersDAO(connection);

            // Grab the user data from the session
            User currentUser = (User) session.getAttribute("currentUser");

            // Get the order list
            try{
                currentOrderBean.setOrderList(ordersDAO.produceOrderList(currentUser.getEmail()));
            } catch (SQLException e){
                currentOrderBean.setOrderList(new ArrayList<>());
            }

            // Set the boolean emptyList to an appropriate value
            boolean emptyList = currentOrderBean.getOrderList().size() == 0;

            startGraphicEngine(request, response, currentOrderBean, emptyList, "/orders.html");
        } else {
            response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToLoginServletPath);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        doGet(request, response);
    }

    private void startGraphicEngine(HttpServletRequest request, HttpServletResponse response, Order orderBean, boolean emptyList, String path) throws ServletException, IOException{

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
        ctx.setVariable("orders", orderBean);
        ctx.setVariable("emptyList", emptyList);
        templateEngine.process(path, ctx, response.getWriter());

    }

}