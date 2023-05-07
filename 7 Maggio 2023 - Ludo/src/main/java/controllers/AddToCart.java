package controllers;

import static utils.TemplateHandler.getEngine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.Cart;
import beans.CartedProduct;
import dao.ProductDAO;
import dao.ProductPricesDAO;
import dao.SupplierDao;
import utils.ConnectionHandler;
import utils.PathUtils;

@WebServlet("/AddToCart")
public class AddToCart extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public AddToCart() {
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
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        HttpSession session = request.getSession();

        Cart currentCart = (Cart) session.getAttribute("currentCart");

        String sellerCode = request.getParameter("supplierCode");
        String supplierName = request.getParameter("supplierName");
        String priceString = request.getParameter("supplierCost");
        String code = request.getParameter("productCode");
        String name = request.getParameter("productName");
        String quantityString = request.getParameter("quantity");

        if(sellerCode == null || priceString == null || code == null || name == null || quantityString == null || supplierName == null) {

            forwardToErrorPage(request,response, "Null product parameter");
            return;

        }
        
        ProductDAO productDao = new ProductDAO(connection);
        
        try {
			if(!productDao.seeProduct(code, name)) {
				forwardToErrorPage(request, response, "Invalid Product in cart");
				return;
			}
		} catch (SQLException e) {
			forwardToErrorPage(request,response,e.getMessage());
			return;
		}
        
        SupplierDao supplierDao = new SupplierDao(connection);
        
        try {
        	if(!supplierDao.seeSupplier(sellerCode, supplierName)) {
				forwardToErrorPage(request, response, "Invalid Seller of product in cart");
				return;
        	}
        }catch(SQLException e) {
			forwardToErrorPage(request,response,e.getMessage());
			return;
        }
        
        ProductPricesDAO productPricesDao = new ProductPricesDAO(connection);
        
        try {
        	float price = productPricesDao.getProductPrice(sellerCode, code);
        	if(Float.parseFloat(priceString) != price) {
				forwardToErrorPage(request, response, "Cost was manipulated!");
				return;
        	}
        }catch(SQLException e) {
			forwardToErrorPage(request,response,e.getMessage());
			return;
        }
        
        boolean alreadyPresent = false;
        float price = Float.parseFloat(priceString);
        int quantity = Integer.parseInt(quantityString);
        CartedProduct product = new CartedProduct();
        product.setSellerCode(sellerCode);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setProductCode(code);
        product.setProductName(name);

        if(currentCart == null) {

            List<CartedProduct> products = new ArrayList<>();
            products.add(product);
            Map<String, List<CartedProduct>> cart = new HashMap<>();
            cart.put(sellerCode, products);
            Cart cartHelp = new Cart();
            cartHelp.setCart(cart);
            currentCart = cartHelp;

        }
        else {

            if(currentCart.getCart() != null) {

                Map<String, List<CartedProduct>> cart = currentCart.getCart();
                List<CartedProduct> products = null;
                if(cart.get(sellerCode) == null) {

                    products = new ArrayList<>();
                    products.add(product);
                    cart.put(sellerCode, products);

                }
                else {

                    for(CartedProduct checkProduct : cart.get(sellerCode)) {

                        if(checkProduct.getProductCode().equals(product.getProductCode())) {

                            int quantityHelper = checkProduct.getQuantity();
                            checkProduct.setQuantity(quantityHelper + product.getQuantity());
                            alreadyPresent = true;

                        }

                    }
                    if(alreadyPresent == false) {

                        cart.get(sellerCode).add(product);
                        currentCart.setCart(cart);

                    }

                }
            }
            else {

                Map<String,List<CartedProduct>> cart = new HashMap<>();
                List<CartedProduct> products = new ArrayList<>();
                products.add(product);
                cart.put(sellerCode, products);
                currentCart.setCart(cart);

            }

        }

        session.setAttribute("currentCart", currentCart);
        response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToCartServletPath);

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