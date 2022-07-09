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



//multiConfig-->annotation to handle multipart/form-data requests
@WebServlet("/CheckRegister")
@MultipartConfig
public class CheckRegister extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private Connection connection=null;
    
	//constructor
    public CheckRegister() {
        super();
    }
    
    public void init() throws UnavailableException {
    	
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doPost(request, response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String name=null;
		String surname=null;
		String username=null;
		String psw=null;
		String pswRep=null;
		String email=null;
		
		
		//catching the five parameters that come from the Login page
		name = request.getParameter("name");	
		surname = request.getParameter("surname");
		username = request.getParameter("username");
		email=request.getParameter("eMail");
		psw = request.getParameter("psw");
		pswRep = request.getParameter("pswRep");
		
		UserDAO userDAO=new UserDAO(connection); //create a UserDAO object
		User user=null;
		

		if (username == null || psw == null || name ==null || surname==null || email==null || surname.isEmpty() 
				|| name.isEmpty() || username.isEmpty() || psw.isEmpty() || email.isEmpty() || pswRep==null || pswRep.isEmpty()) {
			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing/Incorrect Information!");
			return;
		}
		
		//check e-mail
		if(!email.contains("@") || !email.contains(".")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Email format not valid!");
			return;
		}
		
		//check passwords
		if(psw.length() != pswRep.length() || !psw.equals(pswRep)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Password do not match!");
			return;
		}
		
		try {
			user=userDAO.getUserByUsername(username);
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to find user!");
			return;
		}
		
		//user already registered
		if(user!=null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Username already registered!");
			return;
		}
		else {

			try {
				userDAO.registerUser(name,surname,username,psw,email);
			} 
			catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to register user!");
				return;
			}
		}
		
		//check if now is registered
		try {
			user=userDAO.getUserByUsername(username);
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to find user!");
			return;
		}
		
		request.getSession().setAttribute("user", user);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(username);
		
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
