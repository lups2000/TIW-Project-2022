package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/MoveDocument")
public class MoveDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine=null;
       
    //constructor
    public MoveDocument() {
        super();
    }
    
    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    	templateEngine=TemplateHandler.getEngine(servletContext, ".html");
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session=request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath()); //redirect to login page
			return;
		}
		
		User user= (User)session.getAttribute("user");
		Integer documentId=null;
		
		//catching the parameter-->documentId
		try{
			documentId=Integer.parseInt(request.getParameter("documentId"));
		}
		catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters values");
			return;
		}
		
		DocumentDAO documentDAO=new DocumentDAO(connection);
		SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
		FolderDAO folderDAO=new FolderDAO(connection);
		Document document=new Document();
		SubFolder subFolderFather=new SubFolder();
		List<Folder> foldersTree=new ArrayList<Folder>();
		
		//check document existence
		try {
			document=documentDAO.findDocumentById(documentId);
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find document!");
			return;
		}
		
		if(document==null || document.getId_owner()!= user.getId()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,"The document does not exists!");
	    	return;
		}
		
		try {
			foldersTree=folderDAO.findFoldersTreeByUser(user.getId());
			subFolderFather=subFolderDAO.findSubFolderTree(document.getId_subfolder());
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find content!");
			return;
		}

		//now redirect to the HomePage
		
		String path = "WEB-INF/Home.html";
		ServletContext servletContext =getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
		ctx.setVariable("foldersTree", foldersTree);
		ctx.setVariable("document", document);
		ctx.setVariable("user", user);
		ctx.setVariable("moveDoc", "You are moving the document '"+document.getName()+"' from subFolder '"+subFolderFather.getName()+"'. Choose a new subFolder!");
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
