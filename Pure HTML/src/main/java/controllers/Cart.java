package controllers;

import beans.CartSellerNames;
import beans.Preorder;
import beans.PreorderCost;
import dao.ShippingPolicyDAO;
import dao.SupplierDao;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ConnectionHandler;

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
import java.util.HashMap;

import static utils.TemplateHandler.getEngine;

@WebServlet("/Cart")
public class Cart extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Cart() {
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

        // Get current session
        HttpSession session = request.getSession();

        // Control variable to display empty cart in cart.html
        boolean emptyCart;

        // Get the cart of the user
        beans.Cart userCart = (beans.Cart) session.getAttribute("currentCart");

        Preorder costs = null;
        CartSellerNames names = null;

        if(userCart.getCart() == null){
            emptyCart = true;
        } else {
            if(userCart.getCart().isEmpty()){
                emptyCart = true;
            } else {
                emptyCart = false;
                //Get the preorder cost for each seller
                costs = getPreorderCosts(userCart);
                // Get the names for each seller from the seller codes
                names = getSellerNames(userCart);
            }
        }

        // Call the graphic engine
        startGraphicEngine(request, response, userCart, emptyCart, costs, names, "/cart.html");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        doGet(request, response);

    }


    private Preorder getPreorderCosts(beans.Cart cart) {

        final Preorder[] result = {new Preorder()};
        HashMap<String, PreorderCost> costs = new HashMap<>();
        result[0].setCosts(costs);

        for(String id : cart.getCart().keySet()){
            // Create a PreorderCost object
            PreorderCost preorderCost = new PreorderCost();

            // Calculate product costs
            final float[] productCost = {0.0f};
            cart.getCart().get(id).forEach(x -> productCost[0] += x.getPrice() * x.getQuantity());
            preorderCost.setProductsCost(productCost[0]);

            // Calculate delivery fees
            preorderCost.setDeliveryCost(calculateDeliveryCost(id, cart, productCost[0]));


            // Set total cost
            preorderCost.setTotal(preorderCost.getProductsCost() + preorderCost.getDeliveryCost());

            // Add the preorderCost to the result
            result[0].getCosts().put(id, preorderCost);
        }

        return result[0];

    }

    private CartSellerNames getSellerNames(beans.Cart cart){
        CartSellerNames result = new CartSellerNames();
        HashMap<String, String> names = new HashMap<>();
        result.setSellerNames(names);

        for(String id : cart.getCart().keySet()){
            // Instantiate a SupplierDao
            SupplierDao supplierDAO = new SupplierDao(connection);
            try{
                result.getSellerNames().put(id, supplierDAO.findSupplierName(id));
            } catch (SQLException e) {
                result.getSellerNames().put(id, " ");
            }
        }
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

    private void startGraphicEngine(HttpServletRequest request, HttpServletResponse response, beans.Cart cartBean, boolean emptyCart, Preorder costs, CartSellerNames names, String path) throws ServletException, IOException{

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
        ctx.setVariable("cart", cartBean);
        ctx.setVariable("emptyCart", emptyCart);
        ctx.setVariable("costs", costs);
        ctx.setVariable("names", names);
        templateEngine.process(path, ctx, response.getWriter());

    }

}