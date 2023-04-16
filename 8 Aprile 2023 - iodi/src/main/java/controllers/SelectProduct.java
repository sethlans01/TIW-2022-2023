package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.Product;
import beans.Supplier;
import dao.SupplierDao;
import dao.ProductDAO;
import utils.ConnectionHandler;
import utils.PathUtils;
import utils.TemplateHandler;


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
		doPost(request,response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String productCode = request.getParameter("productCode");
		if(productCode == null) {
			forwardToErrorPage(request,response, "No key to search products with!");
			return;
		}
		
		Product product = null;
		List<Supplier> suppliers = new ArrayList<>();
		ProductDAO fullProductDao = new ProductDAO(connection);
		SupplierDao supplierDao = new SupplierDao(connection);
		
		try {
			product = fullProductDao.findProduct(productCode);
			suppliers = supplierDao.findSuppliers(productCode);
			for(int i = 0; i < suppliers.size(); i++) {
				suppliers.get(i).setName(supplierDao.findSupplierName(suppliers.get(i).getCode())); 
				suppliers.get(i).setScore(supplierDao.findSupplierScore(suppliers.get(i).getCode())); 
				suppliers.get(i).setPolicies(supplierDao.findSupplierShips(suppliers.get(i).getCode()));

			}
			
		}catch(SQLException e) {
			forwardToErrorPage(request,response,e.getMessage());
			return;
		}
		
		if(product == null) {
			request.setAttribute("warning", "Code incorrect!");
			forward(request,response, PathUtils.pathToErrorPage);
			return;
		}
		
		startGraphicEngine(request, response, product, suppliers);
		
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