package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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

import it.polimi.tiw.Beans.Document;
import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/ChangeDocumentPosition")
public class ChangeDocumentPosition extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine=null;
	
       
    //constructor
    public ChangeDocumentPosition() {
        super();
    }
	
	public void init() throws UnavailableException {
		ServletContext servletContext=getServletContext();
		connection=ConnectionHandler.getConnection(servletContext);
		templateEngine=TemplateHandler.getEngine(servletContext, ".html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Integer documentId=null;
		Integer nextSubFolderId=null;
		
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath()); //redirect to login page
			return;
		}
		
		User user=(User)session.getAttribute("user");
		
		try {
			documentId=Integer.parseInt(request.getParameter("documentId"));
			nextSubFolderId=Integer.parseInt(request.getParameter("subFolderId"));
		} 
		catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters values");
			return;
		}
		
		//I need the object document in order to get the current subfolder and delete the document from it
		Document document=new Document();
		SubFolderDAO subFolderDAO = new SubFolderDAO(connection);
		DocumentDAO documentDAO=new DocumentDAO(connection);
		FolderDAO folderDAO = new FolderDAO(connection);
		
		try {
			document=documentDAO.findDocumentById(documentId); 
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to find document!");
			return;
		}
		
		//check document existence
		if(document==null || document.getId_owner()!=user.getId()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Document does not exists!");
			return;
		}
		
		//if dest == source
		if(document.getId_subfolder() == nextSubFolderId) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Dest equals to source!");
			return;
		}
				
		//check subFolder existence
	    boolean exists;
	    Folder fatherFolder = new Folder();
	    int fatherFolderId;
	    
	    //find father subFolder id
	    try {
			fatherFolderId = subFolderDAO.findFatherFolderId(nextSubFolderId);
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
	    	response.sendError(HttpServletResponse.SC_NOT_FOUND,"The subFolder does not exists!");
	    	return;
	    }
	    
	    SubFolder subFolderDest= null;
	    try {
			subFolderDest = subFolderDAO.findSubFolderTree(nextSubFolderId);
		} catch (SQLException e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to find subFolder!");
			return;
		}
	    
	    List<Document> documents = subFolderDest.getDocuments();
	    boolean alreadyExists=false;
	    //check for the name
	    for(Document doc : documents) {
	    	if(doc.getName().equals(document.getName())) {
	    		alreadyExists=true;
	    		break;
	    	}
	    }
		
	    if(alreadyExists) {
	    	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Move invalid because there is already a document with the same name in this subFolder!");
			return;
	    }
	    
		try {
			//changing the position of the document
			documentDAO.changeDocumentPosition(documentId,nextSubFolderId);
		} 
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to change document position!");
			return;
		}
		
		//now redirect to the Documents Page of the next SubFolder
		String path=getServletContext().getContextPath()+"/DocumentsPage?subFolderId="+nextSubFolderId;
		response.sendRedirect(path);
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
