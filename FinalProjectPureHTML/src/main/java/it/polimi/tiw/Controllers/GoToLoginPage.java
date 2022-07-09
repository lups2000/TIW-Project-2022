package it.polimi.tiw.Controllers;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.Utils.TemplateHandler;

/**
 * This servlet is the first servlet called when the user run the application.
 * It redirects him to the login Page.
 *
 */
@WebServlet("")
public class GoToLoginPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    
    public GoToLoginPage() {
        super();
    }

    public void init() {
    	ServletContext servletContext=getServletContext();
    	templateEngine=TemplateHandler.getEngine(servletContext, ".html");
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String path="/WEB-INF/Login.html";
		ServletContext servletContext=getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
		templateEngine.process(path, ctx,response.getWriter());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
