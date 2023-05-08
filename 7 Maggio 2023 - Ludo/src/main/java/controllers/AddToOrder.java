package controllers;

import static utils.TemplateHandler.getEngine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beans.Address;
import beans.CartedProduct;
import beans.PreorderCost;
import beans.User;
import dao.OrdersDAO;
import dao.ShippingPolicyDAO;
import dao.UserDao;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import utils.ConnectionHandler;
import utils.PathUtils;

@WebServlet("/AddToOrder")
public class AddToOrder extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public AddToOrder() {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get current session
        HttpSession session = request.getSession();

        String sellerId = request.getParameter("sellerId");
                
        // Get request parameter and check if it's a valid parameter
        if(checkParameter(sellerId, session)){
            // Get user
            User user = (User) session.getAttribute("currentUser");

            // Get Cart
            beans.Cart cart = (beans.Cart) session.getAttribute("currentCart");

            // Add things to db
            addThingsToDB(cart, sellerId, user.getEmail(), request, response);

            // Remove the things from the cart bean
            updateBeans(session, sellerId);

            // Go to Orders page
            response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToOrderServletPath);

        } else {
            forwardToErrorPage(request, response, "Invalid SellerID when making an order");
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);

    }

    private boolean checkParameter(String sellerId, HttpSession session) {

        // Check if the string is a parsable integer
        try{
            int sellerCode = Integer.parseInt(sellerId);
        } catch (NumberFormatException e){
            return false;
        }

        // Check if there is a seller in the user's cart corresponding to that id
        beans.Cart userCart = (beans.Cart) session.getAttribute("currentCart");

       return userCart.getCart().containsKey(sellerId);

    }

    private void addThingsToDB(beans.Cart cart, String sellerId, String user, HttpServletRequest request,
                               HttpServletResponse response) throws ServletException, IOException {

        // Add a tuple in Orders table

        // Get an order code
        OrdersDAO ordersDAO = new OrdersDAO(connection);
        String orderCode;
        try{
            orderCode = ordersDAO.getNewOrderCode(user);
        } catch (SQLException e){
            orderCode = " ";
        }

        // Get order's cost
        PreorderCost orderCost = getOrderCost(cart, sellerId, user);

        // Get today's date for the shipping
        LocalDate todayDate = LocalDate.now();
        Date correctDate = Date.valueOf(todayDate);

        // Get address
        Address userAddress;

        // Get a UserDAO
        UserDao userDao = new UserDao(connection);

        try{
            // Get address things
            userAddress = userDao.getUserAddress(user);

            // Add order into db
            ordersDAO.addOrder(orderCode, sellerId, orderCost.getTotal(), correctDate, userAddress.getStreetName(),
                               userAddress.getStreetNumber(), userAddress.getCity(), userAddress.getProvince(),
                               userAddress.getCAP(), user);
        } catch(SQLException e){
            forwardToErrorPage(request, response, "Error inserting an order in db");
        }

        // Get the list of the carted products that need to be ordered
        List<CartedProduct> productsToAdd = cart.getCart().get(sellerId);

        // Add the products to OrderedProducts table
        for(CartedProduct cp : productsToAdd){
            try{
                ordersDAO.addOrderedProduct(orderCode, cp.getProductCode(), cp.getQuantity(), user);
            } catch (SQLException e){
                forwardToErrorPage(request, response, "Error inserting a product in order db");
            }
        }

    }

    private PreorderCost getOrderCost(beans.Cart cart, String sellerId, String user){

        // Create the result object
        PreorderCost result = new PreorderCost();

        // Calculate productsCost
        float productsCost = 0.0f;
        for(CartedProduct cartedProduct : cart.getCart().get(sellerId)){
            productsCost += cartedProduct.getPrice() * cartedProduct.getQuantity();
        }
        result.setProductsCost(productsCost);

        // Calculate delivery fee
        result.setDeliveryCost(calculateDeliveryCost(sellerId, cart, productsCost));

        // Calculate total
        result.setTotal(result.getProductsCost() + result.getDeliveryCost());

        return result;
    }

    private float calculateDeliveryCost(String sellerId, beans.Cart cart, float productCost){

        // Instantiate a ShippingPolicyDAO
        ShippingPolicyDAO shippingPolicyDAO = new ShippingPolicyDAO(connection);

        // Check if the productCost total is greater than the free shipping threshold
        float freeThreshold;
        try{
            freeThreshold = shippingPolicyDAO.getFreeShippingThreshold(sellerId);
        } catch(SQLException e){
            freeThreshold = 0.0f;
        }
        if(productCost >= freeThreshold){
            return 0.0f;
        }

        // Since the total cost is not greater than the free shipping threshold, calculate the delivery cost
        // by summing the delivery costs of each product in the cart
        final int[] totalAmountOfProducts = {0};
        cart.getCart().get(sellerId).forEach(x -> totalAmountOfProducts[0] += x.getQuantity());
        float result;
        try{
            result = shippingPolicyDAO.calculateShippingFee(totalAmountOfProducts[0], sellerId);
        } catch (SQLException e){
            result = 0.0f;
        }

        return result;

    }


    private void updateBeans(HttpSession session, String sellerId){

        // Update cart
        beans.Cart cart = (beans.Cart) session.getAttribute("currentCart");
        cart.getCart().remove(sellerId);

    }

    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error)throws ServletException, IOException{
        request.setAttribute("error", error);
        forward(request, response, PathUtils.pathToErrorPage);
        return;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
        templateEngine.process(path,ctx,response.getWriter());
    }

}