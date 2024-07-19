package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import beans.Homepage;
import beans.User;
import dao.LastFiveDAO;
import dao.UserDAO;
import utils.ConnectionHandler;


/**
 * Servlet implementation class ToRegisterPage
 */
@WebServlet("/Home")
@MultipartConfig
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Home() {
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(false);
		
        if(checkAccess(session)){
            // Create a DAO to access DB
            LastFiveDAO u5DAO = new LastFiveDAO(connection);
            // Get username from the context
            User current = (User) session.getAttribute("currentUser");
            // Get the bean from DAO
            Homepage currentHomepageBean;
            try {
                currentHomepageBean = u5DAO.getHomepageBean(current.getEmail());
            } catch (SQLException e) {
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			response.getWriter().println(e.getMessage());
    			return;           
    			}
            // Add user's full name to the bean
            UserDAO userDao = new UserDAO(connection);
            try {
                currentHomepageBean = userDao.getFullName(currentHomepageBean);
            } catch (SQLException e) {
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			response.getWriter().println(e.getMessage());
    			return;          
    			}
            
    		String json = new Gson().toJson(currentHomepageBean);
    		
    		response.setContentType("application/json");
    		response.setCharacterEncoding("UTF-8");
    		response.getWriter().write(json);
    		
        } else {
        }
         
	}
	
    // Check if the current client is logged in
    private boolean checkAccess(HttpSession session) throws ServletException{

        User current = (User) session.getAttribute("currentUser");
        return current != null;

    }
}