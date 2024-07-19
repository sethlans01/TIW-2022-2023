package controllers;

import beans.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.OrdersDAO;
import dao.ShippingPolicyDAO;
import dao.UserDAO;
import utils.ConnectionHandler;
import utils.HTTPResponseCodes;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static utils.CartChecker.checkCart;

@WebServlet("/AddToOrders")
public class AddToOrders extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Connection connection;

    public AddToOrders() {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Grab current session
        HttpSession session = request.getSession();

        // Grab current user
        User currentUser = (User) session.getAttribute("currentUser");

        // Get parameters from the request
        String serializedCart = request.getParameter("cart");
        String seller = request.getParameter("seller");

        // Create a gson object
        Gson gson = new Gson();

        // Create a TypeToken to describe the data structure I want the cart to be deserialized into
        Type mapType = new TypeToken<Map<String, List<CartedProduct>>>(){}.getType();

        // Deserialize the cart
        Cart cart = gson.fromJson(serializedCart, new TypeToken<Cart>(){}.getType());
        cart.setCart(gson.fromJson(serializedCart, mapType));


        // Check if there isn't any illegal product in the cart
        if(!checkCart(cart, connection)){
            response.setStatus(HTTPResponseCodes.SC_TEAPOT);
            return;
        }

        // Check that the seller you want to make an order from is present in the cart
        boolean sellerPresent;
        sellerPresent = cart.getCart().containsKey(seller);

        if(!sellerPresent){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Add things to db
        addThingsToDB(cart, seller, currentUser.getEmail(), response);

        response.setStatus(HttpServletResponse.SC_OK);

    }

    private void addThingsToDB(Cart cart, String sellerId, String user,
                               HttpServletResponse response) {

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
        UserDAO userDao = new UserDAO(connection);

        // Get the list of the carted products that need to be ordered
        List<CartedProduct> productsToAdd = cart.getCart().get(sellerId);

        try{
            // Get address things
            userAddress = userDao.getUserAddress(user);

            // Add order into db
            ordersDAO.addOrder(orderCode, sellerId, orderCost.getTotal(), correctDate, userAddress.getStreetName(),
                    userAddress.getStreetNumber(), userAddress.getCity(), userAddress.getProvince(),
                    userAddress.getCAP(), user, productsToAdd);
        } catch(SQLException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

    }

    public static PreorderCost getOrderCost(Cart cart, String sellerId, String user){

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

    public static float calculateDeliveryCost(String sellerId, Cart cart, float productCost){

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

}