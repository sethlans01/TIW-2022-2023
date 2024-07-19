package controllers;

import beans.*;
import dao.ShippingPolicyDAO;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import utils.ConnectionHandler;
import utils.HTTPResponseCodes;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.CartChecker.checkCart;

@WebServlet("/VerifyCart")
public class VerifyCart extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Connection connection;

    public VerifyCart() {
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

        // Calculate the cart prices
        // Instantiate the CartPrices object
        CartPrices cartPrices = new CartPrices();
        cartPrices.setCartCosts(new HashMap<>());

        // Calculate the cost for each seller present in the cart
        for(String key : cart.getCart().keySet()){
            cartPrices.getCartCosts().put(key, getOrderCost(cart, key, currentUser.getEmail()));
        }

        String serializedCartPrices = new Gson().toJson(cartPrices);

        // Send the response
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(serializedCartPrices);

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