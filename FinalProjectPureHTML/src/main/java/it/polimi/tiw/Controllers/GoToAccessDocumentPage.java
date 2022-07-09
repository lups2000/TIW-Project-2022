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
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.Beans.Document;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/DocumentPage")
public class GoToAccessDocumentPage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    //constructor
    public GoToAccessDocumentPage() {
        super();
    }

    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    	templateEngine=TemplateHandler.getEngine(servletContext,".html");
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath()); //redirect to login page
			return;
		}
		
		User user = (User)session.getAttribute("user");
		DocumentDAO documentDAO = new DocumentDAO(connection);
		Document document = new Document();
		Integer documentId=null;
		
		//catching the parameter-->documentId
		try{
			documentId=Integer.parseInt(request.getParameter("documentId"));
		}
		catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters values");
			return;
		}
		
		//check the existence of the document
		try {
			document=documentDAO.findDocumentById(documentId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find Document!");
			return;
		}
		
		if(document == null || document.getId_owner() != user.getId()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,"The document does not exist!");
	    	return;
		}
		
		//now redirect to the Document page
		String path = "WEB-INF/Document.html";
		ServletContext servletContext =getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
		ctx.setVariable("document", document);
		ctx.setVariable("user", user);
		templateEngine.process(path, ctx,response.getWriter());
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
