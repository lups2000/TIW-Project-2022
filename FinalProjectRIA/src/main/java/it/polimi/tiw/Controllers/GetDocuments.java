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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;



@WebServlet("/GetDocuments")
public class GetDocuments extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public void init() throws UnavailableException {
		ServletContext servletContext=getServletContext();
		connection=ConnectionHandler.getConnection(servletContext);
	}
	
    //constructor
    public GetDocuments() {
        super();
    }

    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		
		if(session.isNew() || user == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	    
	    SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
	    SubFolder subFolder=new SubFolder();
	    FolderDAO folderDAO= new FolderDAO(connection);
	    Integer subFolderId=null;
	    
	    //catching the parameter from the request
	    try {
	    	subFolderId=Integer.parseInt(request.getParameter("subFolderId"));
	    }
	    catch (NumberFormatException | NullPointerException e) {
	    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover documents");
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
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not Possible to find father Folder id!");
			return;
		}
	    //find father Folder
	    try {
			fatherFolder = folderDAO.findFolderById(fatherFolderId);
		} catch (SQLException e1) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not Possible to find father Folder!");
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
	    	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    	response.getWriter().println("SubFolder does no exist!");
			return;
	    }
	    
	    try {
			subFolder=subFolderDAO.findSubFolderTree(subFolderId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    	response.getWriter().println("Not possible to find subFolder!");
			return;
		}
	    
	    Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String jsonString = gson.toJson(subFolder.getDocuments());
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonString);//serialize 
		
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
