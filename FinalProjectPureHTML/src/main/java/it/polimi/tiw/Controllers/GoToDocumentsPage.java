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


import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.FolderDAO;
//import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/DocumentsPage")
public class GoToDocumentsPage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
	
	public void init() throws UnavailableException {
		ServletContext servletContext=getServletContext();
		connection=ConnectionHandler.getConnection(servletContext);
		templateEngine=TemplateHandler.getEngine(servletContext, ".html");
	}
	
    //constructor
    public GoToDocumentsPage() {
        super();
    }

    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath()); //redirect to login page
			return;
		}
		
		User user = (User)session.getAttribute("user");
	    SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
	    FolderDAO folderDAO = new FolderDAO(connection);
	    SubFolder subFolder=new SubFolder();
	    Integer subFolderId=null;
	    
	    try {
	    	subFolderId=Integer.parseInt(request.getParameter("subFolderId"));
	    }
	    catch (NumberFormatException | NullPointerException e) {
	    	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters values!");
			return;
		}
	    
	    //check the existence of the subFolder
	    boolean exists;
	    Folder fatherFolder = new Folder();
	    int fatherFolderId;
	    
	    //find father subFolder id
	    try {
			fatherFolderId = subFolderDAO.findFatherFolderId(subFolderId);
		} catch (SQLException e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find father Folder id!");
			return;
		}
	    //find father Folder
	    try {
			fatherFolder = folderDAO.findFolderById(fatherFolderId);
		} catch (SQLException e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find father Folder!");
			return;
		}
	    
	    //there is no fatherFolder
	    if(fatherFolder==null) {
	    	exists=false;
	    }
	    else { //there is a fatherFolder
	    	
	    	if(fatherFolder.getId_owner() != user.getId()) {
		    	exists=false;
		    }
		    else {
		    	exists=true;
		    }
	    }
	    if(!exists) {
	    	response.sendError(HttpServletResponse.SC_NOT_FOUND,"The subFolder does not exist!");
	    	return;
	    }
	    
	    try {
			subFolder=subFolderDAO.findSubFolderTree(subFolderId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find subFolder!");
			return;
		}
	    
	    //now redirect to the Documents Page
  		String path="/WEB-INF/Documents.html";
  		ServletContext servletContext=getServletContext();
  		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
  		ctx.setVariable("subFolder", subFolder);
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
