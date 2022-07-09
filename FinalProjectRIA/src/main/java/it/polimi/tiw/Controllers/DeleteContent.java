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
import javax.servlet.http.HttpSession;

import it.polimi.tiw.Beans.Document;
import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


@WebServlet("/DeleteContent")
@MultipartConfig
public class DeleteContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    
    public DeleteContent() {
        super();
    }
    
    public void init() throws UnavailableException {

    	connection=ConnectionHandler.getConnection(getServletContext());
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
		String summary = request.getParameter("summary");
		String type = request.getParameter("type");
		String userIdString = request.getParameter("userId");
		String objectIdString = request.getParameter("objectId");
		
		Integer objectId=-1;
		Integer userId=-1;
		
		try {
			objectId = Integer.parseInt(objectIdString);
		}
		catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect: Bad Request!");
			return;
		}
		
		if(!userIdString.isEmpty()) {
			try {
				userId = Integer.parseInt(userIdString);
			}
			catch (NumberFormatException | NullPointerException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Incorrect: Bad Request!");
				return;
			}
		}
	
		//delete a folder
		if(userId != -1 && summary.isEmpty() && type.isEmpty()) {
			FolderDAO folderDAO = new FolderDAO(connection);
			Folder folder = new Folder();
			
			try {
				folder=folderDAO.findFolderById(objectId);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to recover folder!");
				return;
			}
			
			//check user
			if(userId!=folder.getId_owner() && user.getId() != userId) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Unauthorized!Session Ended!");
				return;
			}
			else {
				try {
					folderDAO.deleteFolder(objectId);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Not possible to delete folder!");
					return;
				}
			}
		}
		
		//delete a subFolder
		else if(userId ==-1 && summary.isEmpty() && type.isEmpty()) {
			SubFolderDAO subFolderDAO = new SubFolderDAO(connection);
			FolderDAO folderDAO = new FolderDAO(connection);
			SubFolder subFolder = new SubFolder();
		    Folder father_folder = new Folder();
			
			try {
				subFolder=subFolderDAO.findSubFolderTree(objectId);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to recover subFolder!");
				return;
			}
			
			try {
				father_folder=folderDAO.findFolderById(subFolder.getId_folder());
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to recover father folder!");
				return;
			}
			
			//check user
			if(user.getId() != father_folder.getId_owner()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Unauthorized!Session Ended!");
				return;
			}
			else {
				try {
					subFolderDAO.deleteSubFolder(objectId);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Not possible to delete subFolder!");
					return;
				}
			}
		}
		//delete a document
		else if(userId!=-1 && !summary.isEmpty() && !type.isEmpty()) {
			DocumentDAO documentDAO = new DocumentDAO(connection);
			Document document = new Document();
			
			try {
				document=documentDAO.findDocumentById(objectId);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to recover document!");
				return;
			}
			
			//check user
			if(user.getId() != document.getId_owner()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Unauthorized!Session Ended!");
				return;
			}
			else {
				try {
					documentDAO.deleteDocument(objectId);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Not possible to delete document!");
					return;
				}
			}
			
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect: Bad Request!");
			return;
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
