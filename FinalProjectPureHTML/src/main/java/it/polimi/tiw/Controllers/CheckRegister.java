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


@WebServlet("/CheckRegister")
public class CheckRegister extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private Connection connection=null;
    private TemplateEngine templateEngine;
    
	//constructor
    public CheckRegister() {
        super();
    }
    
    public void init() throws UnavailableException {
    	
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    	templateEngine=TemplateHandler.getEngine(servletContext, ".html");
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doPost(request, response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String name=null;
		String surname=null;
		String username=null;
		String psw=null;
		
		ServletContext servletContext = getServletContext();
		String path=null;
		
		//catching the four parameters that come from the Register form
		name = (request.getParameter("name")).toLowerCase();	
		surname = (request.getParameter("surname")).toLowerCase();
		username = request.getParameter("username");
		psw = request.getParameter("psw");
		
		
		if (username == null || psw == null || name ==null || surname==null || surname.isEmpty() || name.isEmpty() || username.isEmpty() || psw.isEmpty()) {
			
			final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
			ctx.setVariable("errorMsg", "Missing information");
			path="/WEB-INF/Register.html";
			templateEngine.process(path, ctx,response.getWriter());
			return;
		}
		
		UserDAO userDAO=new UserDAO(connection); //create a UserDAO object
		User user=null;
		
		try {
			user=userDAO.getUserByUsername(username);
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to recover the user");
			return;
		}
		
		//user already registered
		if(user!=null) {
			final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
			ctx.setVariable("errorMsg", "User already registered");
			path = "/WEB-INF/Register.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		else {
			try {
				userDAO.registerUser(name,surname,username,psw);
			} 
			catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to register the user!");
				return;
			}
		}
		
		//check
		try {
			user=userDAO.getUserByUsername(username);
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to Check the username");
			return;
		}
		
		request.getSession().setAttribute("user", user);
		path = getServletContext().getContextPath() + "/HomePage";
		response.sendRedirect(path); //redirect to the HomePage 
		
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
