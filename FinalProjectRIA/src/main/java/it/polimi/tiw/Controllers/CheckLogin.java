package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.UserDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection=null;
       
	//constructor
    public CheckLogin() {
        super();
    }
    
    //method to initialize the Servlet
    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext(); //Servlet context is contained in Web.xml,it defines the interface of a Servlet 
    	connection = ConnectionHandler.getConnection(servletContext); //the connection is set
    	
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username=null;
		String psw=null;
		
		//catching the two parameters that come from the Login form
		username = request.getParameter("username");
		psw = request.getParameter("psw");
		
		
		if (username == null || psw == null || username.isEmpty() || psw.isEmpty()) {

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials cannot be empty!");
			return;
		}

		
		UserDAO userDAO=new UserDAO(connection); //create a UserDAO object
		User user=null;
		
		try {
			
			user = userDAO.checkCredentials(username, psw);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to check credentials!");
			return;
		}
		
		// If the user exists, add info to the session and go to home page, otherwise
		// show login page with error message
		
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Username or Password incorrect!");
			return;
		} 
		else {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(username);
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
