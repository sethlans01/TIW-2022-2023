package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
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
import dao.ProductDAO;
import utils.ConnectionHandler;
import utils.PathUtils;
import utils.TemplateHandler;


@WebServlet("/Search")
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Search() {
        super();
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

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    			doPost(request,response);
    	}

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
    		String searchKey = request.getParameter("search");
    		
    		if(searchKey == null) {
    			forwardToErrorPage(request,response, "No key to search products with!");
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
    			forwardToErrorPage(request,response,e.getMessage());
    			return;
    		}
    		
    		Collections.sort(products, new Comparator<Product>() {
                @Override
                public int compare(Product p1, Product p2) {
                    return Float.compare(Float.parseFloat(p1.getMinCost()),Float.parseFloat(p2.getMinCost()));
                }
            });
    		
    		startGraphicEngine(request, response, products);
    		
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

        private void startGraphicEngine(HttpServletRequest request, HttpServletResponse response, List<Product> products) throws ServletException, IOException{

            String path = PathUtils.pathToSearchPage;
            String moment = "1";
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
            ctx.setVariable("products", products);
            ctx.setVariable("moment", moment);
            templateEngine.process(path, ctx, response.getWriter());

        }


}