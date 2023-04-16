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
		
        HttpSession session = request.getSession();
        
        Cart currentCart = (Cart) session.getAttribute("currentCart");
        
        String seller = request.getParameter("supplierName");
        String priceString = request.getParameter("supplierCost");
        String code = request.getParameter("productCode");
        String name = request.getParameter("productName");
        String quantityString = request.getParameter("quantity");
        
        if(seller == null || priceString == null || code == null || name == null || quantityString == null) {
        	
			forwardToErrorPage(request,response, "Null product parameter");
			return;
        	
        }
        
        float price = Float.parseFloat(priceString);
        int quantity = Integer.parseInt(quantityString);
        CartedProduct product = new CartedProduct();
        product.setSellerName(seller);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setProductCode(code);
        product.setProductName(name);
        
        if(currentCart == null) {
        	
        	List<CartedProduct> products = new ArrayList<>();
        	products.add(product);
        	Map<String, List<CartedProduct>> cart = new HashMap<>();
        	cart.put(seller, products);
        	Cart cartHelp = new Cart(); 
        	cartHelp.setCart(cart);
        	currentCart = cartHelp;
        }
        else {
        	
        	Map<String, List<CartedProduct>> cart = currentCart.getCart();
        	List<CartedProduct> products = cart.get(seller);
        	products.add(product);
        	currentCart.setCart(cart);
        	
        }
		
        session.setAttribute("currentCart", currentCart);
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToCartServletPath);
        
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
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