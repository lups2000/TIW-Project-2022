package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.UserDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;

/**
 * This servlet checks the Login.
 *
 */
@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection=null;
	private TemplateEngine templateEngine;
       
	//constructor
    public CheckLogin() {
        super();
    }
    
    //method to initialize the Servlet
    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext(); //Servlet context is contained in Web.xml,it defines the interface of a Servlet 
    	connection = ConnectionHandler.getConnection(servletContext); //the connection is set
    	
    	//Thymeleaf setup
    	templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username=null;
		String psw=null;
		ServletContext servletContext = getServletContext();
		String path=null;
		
		
		//catching the two parameters that come from the Login form
		username = request.getParameter("username");
		psw = request.getParameter("psw");
		if (username == null || psw == null || username.isEmpty() || psw.isEmpty()) {

			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg", "Missing username or password");
			path = "/WEB-INF/Login.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}

		
		UserDAO userDAO=new UserDAO(connection); //create a UserDAO object
		User user=null;
		
		try {
			
			user = userDAO.checkCredentials(username, psw);
			
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials!");
			return;
		}
		
		// If the user exists, add info to the session and go to home page, otherwise
		// show login page with error message
		
		if (user == null) { 
			//the WebContext object manages the paths for the Web Application
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg", "Incorrect username or password");
			path = "/WEB-INF/Login.html";
			templateEngine.process(path, ctx, response.getWriter());
		} 
		else {
			
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/HomePage";
			response.sendRedirect(path); //redirect to the HomePage
		}
		
	}
	
	//method to destroy the Servlet
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
