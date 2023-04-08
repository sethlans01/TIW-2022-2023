package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beans.Homepage;
import beans.User;
import dao.LastFiveDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import utils.ConnectionHandler;
import utils.PathUtils;

import static utils.TemplateHandler.getEngine;

@WebServlet("/Home")
public class Home extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Home() {
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
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        // Grab current session
        HttpSession session = request.getSession();

        //if(checkAccess(session)){
            // Create a DAO to access DB
            LastFiveDAO u5DAO = new LastFiveDAO(connection);
            // Get username from the context
            User current = (User) session.getAttribute("currentUser");
            // Get the bean from DAO
            Homepage currentHomepageBean;
            try {
                currentHomepageBean = u5DAO.getHomepageBean(current.getEmail());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // Call view engine
            startGraphicEngine(request, response, currentHomepageBean);
        //} else {
        //    response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToLoginPage);
        //}

    }

    // Check if the current client is logged in
    private boolean checkAccess(HttpSession session) throws ServletException{

        User current = (User) session.getAttribute("currentUser");
        return current == null;

    }

    private void startGraphicEngine(HttpServletRequest request, HttpServletResponse response, Homepage homepageBean) throws ServletException, IOException{

        String path = "/home.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
        ctx.setVariable("homepage", homepageBean);
        templateEngine.process(path, ctx, response.getWriter());

    }

}
