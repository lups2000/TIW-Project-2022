package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.Beans.Document;
import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


@WebServlet("/MoveDocument")
@MultipartConfig
public class MoveDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    //constructor
    public MoveDocument() {
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

		HttpSession session=request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		User user= (User)session.getAttribute("user");
		Integer documentId=null;
		Integer subFolderDestId =null;
		DocumentDAO documentDAO=new DocumentDAO(connection);
		SubFolderDAO subFolderDAO = new SubFolderDAO(connection);
		FolderDAO folderDAO = new FolderDAO(connection);
		Folder folderDest = new Folder();
		SubFolder subFolderDest = new SubFolder();
		Document document = new Document();
		
		
		//catching the parameter-->documentId
		try{
			documentId=Integer.parseInt(request.getParameter("documentId"));
			subFolderDestId=Integer.parseInt(request.getParameter("subFolderDestId"));
		}
		catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect: Bad Request!");
			return;
		}
		

		//check that the origin is a document and the destination a subFolder
		
		//check SubFolder destination
		
		try {
			subFolderDest = subFolderDAO.findSubFolderTree(subFolderDestId);
			document = documentDAO.findDocumentById(documentId);
			folderDest = folderDAO.findFolderById(subFolderDest.getId_folder());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover content!");
			return;
		}
		
		
		//check if the origin is equals or not from the dest
		if(subFolderDest.getId()==document.getId_subfolder()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.getWriter().println("Destination equals to origin!");
			return;
		}
		
		//check user
		if(document.getId_owner()!= user.getId() || user.getId() != folderDest.getId_owner()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Not Possible to move the document!");
			return;
		}
		
		boolean alreadyPresent=false;
		List<Document> documents = subFolderDest.getDocuments();
		
		
		//check for the name
		for(Document doc : documents) {

			if(doc.getName().equals(document.getName())) {
				alreadyPresent = true;
				break;
			}
		}
		
		if(alreadyPresent) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.getWriter().println("Document with the same name already present in the subFolder!");
			return;
		}
		else {
			try {
				documentDAO.changeDocumentPosition(documentId, subFolderDestId);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not Possible to move the document!");
				return;
			}
		}
	    response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
