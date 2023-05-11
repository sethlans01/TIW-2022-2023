package controllers;

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
import beans.Product;
import beans.Supplier;
import beans.User;
import dao.SupplierDao;
import dao.UpdateLastDao;
import dao.ProductDAO;
import utils.ConnectionHandler;
import utils.PathUtils;
import utils.TemplateHandler;


//Servlet called when user wants to see more information about product 
//with all its sellers

@WebServlet("/SelectProduct")
public class SelectProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SelectProduct() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
	public void init() throws ServletException{
    	ServletContext servletContext = getServletContext();
    	this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    	this.connection = ConnectionHandler.getConnection(servletContext);
    }

    @Override
	public void destroy() {
    	try {
    			ConnectionHandler.closeConnection(connection);
    	}catch(SQLException e) {
    		e.printStackTrace();
    	}
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
    	HttpSession session = request.getSession();
		
			
			//Check validity of parameters. Redirects to error page otherwise.
			
			String productCode = request.getParameter("productCode");
	        User currentUser = (User) session.getAttribute("currentUser");
	        Cart pageCart = (Cart) session.getAttribute("currentCart");
			
			if(productCode == null) {
				
				forwardToErrorPage(request,response, "No key to search products with!");
				return;
				
			}
			
	        if(!productCode.matches("[0-9]+")) {
	        	
	            forwardToErrorPage(request,response, "Invalid product code!");
	            return;
	        	
	        }
	        
			//Prepares needed variables needed and daos
			
			Product product = null;
			List<Supplier> suppliers = new ArrayList<>();
			ProductDAO fullProductDao = new ProductDAO(connection);
			SupplierDao supplierDao = new SupplierDao(connection);
			UpdateLastDao updateLastDao = new UpdateLastDao(connection);
			
			try {
				
				float valProd = 0;
				int numProd = 0;
				
				//Find product information
				
				product = fullProductDao.findProduct(productCode);
				
				//Check the product used as input was found in DB. Redirects to error page otherwise
			
				if(product == null) {
				
					request.setAttribute("warning", "Code incorrect!");
					forward(request,response, PathUtils.pathToErrorPage);
					return;
				
				}
				
				//Updates last five seen products
				
				updateLastDao.updateLastFive(currentUser.getEmail(), productCode);
				
				//Finds the suppliers selling the item and retrieves all neeeded information
				
				suppliers = supplierDao.findSuppliers(productCode);
				
				for(int i = 0; i < suppliers.size(); i++) {
					
					//Retrieves information that can be found in DB.
					
					valProd = 0;
					numProd = 0;
					suppliers.get(i).setName(supplierDao.findSupplierName(suppliers.get(i).getCode())); 
					suppliers.get(i).setScore(supplierDao.findSupplierScore(suppliers.get(i).getCode())); 
					suppliers.get(i).setPolicies(supplierDao.findSupplierShips(suppliers.get(i).getCode()));
					
					//Retrieves information regarding the cart and the seller products already in it.
					
					if(pageCart.getCart() != null) {
	
						if(pageCart.getCart().get(suppliers.get(i).getCode()) == null) {
							
							suppliers.get(i).setValProducts("0");
							suppliers.get(i).setNumProducts("0");
							
						}
						else {
							
							for(CartedProduct cartedProduct : pageCart.getCart().get(suppliers.get(i).getCode())) {
								
								valProd += cartedProduct.getPrice()*cartedProduct.getQuantity();
								numProd += cartedProduct.getQuantity();
								
							}
							
							suppliers.get(i).setValProducts(Float.toString(valProd));
							suppliers.get(i).setNumProducts(Integer.toString(numProd));
							
						}
						
					}
					else {
						
						Map<String, List<CartedProduct>> cart = new HashMap<>(); 
						pageCart.setCart(cart);
						suppliers.get(i).setValProducts("0");
						suppliers.get(i).setNumProducts("0");
						
					}
					
				}
			}catch(SQLException e) {
				
				forwardToErrorPage(request,response,e.getMessage());
				return;
				
			}
			
	
			//Method that loads next with required context variables.
			
			startGraphicEngine(request, response, product, suppliers);
			
		

		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
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

    private void startGraphicEngine(HttpServletRequest request, HttpServletResponse response, Product product, List<Supplier> suppliers) throws ServletException, IOException{

        String path = PathUtils.pathToSearchPage;
        String moment = "2";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
        ctx.setVariable("selectedProduct", product);
        ctx.setVariable("suppliers", suppliers);
        ctx.setVariable("moment", moment);
        templateEngine.process(path, ctx, response.getWriter());

    }

}
