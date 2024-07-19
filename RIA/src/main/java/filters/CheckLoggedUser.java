package filters;

import java.io.IOException;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import utils.PathUtils;

/**
 * Servlet Filter implementation class UserFilter
 */

public class CheckLoggedUser implements Filter {
	
    /**
     * Default constructor. 
     */
    public CheckLoggedUser() {
        // TODO Auto-generated constructor stub
    }
    


	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession s = req.getSession(false);
		
		if(s != null) {
			Object user = s.getAttribute("currentUser");
			if(user != null) {
				chain.doFilter(request, response);
				return;
			}
		} 
		
		res.sendRedirect(req.getServletContext().getContextPath() + PathUtils.pathToLoginPage);
	}


}