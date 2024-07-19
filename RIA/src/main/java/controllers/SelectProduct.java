package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;


import beans.Product;
import beans.ProductSuppliers;
import beans.Supplier;
import beans.User;
import dao.SupplierDAO;
import dao.UpdateLastDAO;
import dao.ProductDAO;
import utils.ConnectionHandler;


@WebServlet("/SelectProduct")
@MultipartConfig
public class SelectProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
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
		
		String productCode = request.getParameter("productCode");
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");
		
		if(productCode == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Missing parameter");
			return;
		}
		
        if(!productCode.matches("[0-9]+")) {
        	
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Invalid parameter");
			return;
        	
        }
		
		Product product = null;
		List<Supplier> suppliers = new ArrayList<>();
		ProductDAO fullProductDao = new ProductDAO(connection);
		SupplierDAO supplierDao = new SupplierDAO(connection);
		UpdateLastDAO updateLastDao = new UpdateLastDAO(connection);
		
		try {

			product = fullProductDao.findProduct(productCode);
			
			if(product == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
				response.getWriter().println("Wrong Code");
				return;
			}
			
			updateLastDao.updateLastFive(currentUser.getEmail(), productCode);
			suppliers = supplierDao.findSuppliers(productCode);
			
			for(int i = 0; i < suppliers.size(); i++) {
				
				suppliers.get(i).setName(supplierDao.findSupplierName(suppliers.get(i).getCode())); 
				suppliers.get(i).setScore(supplierDao.findSupplierScore(suppliers.get(i).getCode())); 
				suppliers.get(i).setPolicies(supplierDao.findSupplierShips(suppliers.get(i).getCode()));
				
				
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}

		ProductSuppliers productSuppliers = new ProductSuppliers();
		productSuppliers.setProduct(product);
		productSuppliers.setSuppliers(suppliers);
		
		String json = new Gson().toJson(productSuppliers);
				
		response.setStatus(HttpServletResponse.SC_OK);	
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(json);
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}