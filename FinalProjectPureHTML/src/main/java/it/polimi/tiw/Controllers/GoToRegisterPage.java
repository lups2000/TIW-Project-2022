package it.polimi.tiw.Controllers;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/RegisterPage")
public class GoToRegisterPage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
       
   //constructor
    public GoToRegisterPage() {
        super();
    }

    //method to initialize the Servlet
    public void init() throws UnavailableException {
    	
    	ServletContext servletContext=getServletContext();
    	templateEngine=TemplateHandler.getEngine(servletContext, ".html");
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String path="/WEB-INF/Register.html";
		ServletContext servletContext=getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}
	


}
