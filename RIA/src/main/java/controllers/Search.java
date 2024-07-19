package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.Product;
import dao.ProductDAO;
import utils.ConnectionHandler;


/**
 * Servlet implementation class Login
 */
@WebServlet("/Search")
@MultipartConfig
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    @Override
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String searchKey = request.getParameter("search");
				
		if(searchKey == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Missing parameter");
			return;
		}	
				
		ProductDAO productCostDao = new ProductDAO(connection);
		List<Product> products= null;
	
		
		try {
			products = productCostDao.findProducts(searchKey);
			for(int i = 0; i < products.size();i++){
				products.get(i).setMinCost(productCostDao.findMinCost(products.get(i).getCode()));
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		List<Integer> toRemove = new ArrayList<>();
		
		for(int i = 0; i < products.size(); i++) {
			
			if(products.get(i).getMinCost() == null) {
				toRemove.add(i);
			}
			
		}
		
		Collections.sort(toRemove, new Comparator<Integer>() {
		   public int compare(Integer a, Integer b) {
		      return b.compareTo(a);
		   }
		});
		
		for(int i = 0; i < toRemove.size();i++) {
			
			int index = toRemove.get(i);
			products.remove(index);
			
		}
		
		Collections.sort(products, new Comparator<Product>() {
	        @Override
	        public int compare(Product p1, Product p2) {
	            return Float.compare(Float.parseFloat(p1.getMinCost()),Float.parseFloat(p2.getMinCost()));
	        }
	    });
		
		String json = new Gson().toJson(products);
		
		response.setStatus(HttpServletResponse.SC_OK);	
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(json);
	
		}
}




















