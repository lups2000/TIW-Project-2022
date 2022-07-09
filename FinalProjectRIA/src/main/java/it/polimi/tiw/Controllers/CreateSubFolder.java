package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;
import java.util.List;
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


import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


@WebServlet("/CreateSubFolder")
@MultipartConfig
public class CreateSubFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    //constructor
    public CreateSubFolder() {
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
		
		Integer folderId=-1;
		String nameSubFolder=null;
		Date dateCreation=null;
		String foldIdParam=request.getParameter("folderId");
		List<Folder> allTopFolders=new ArrayList<Folder>();
		List<SubFolder> allSubFolders = new ArrayList<SubFolder>();
		FolderDAO folderDAO=new FolderDAO(connection);
		
		boolean isBadRequest=false;
		boolean isNameOk=true;
		
		if (foldIdParam == null) {
			isBadRequest = true;
		}
		
		HttpSession session=request.getSession();
		if(session.isNew() || session.getAttribute("user")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("session ended");
			return;
		}
		
		User user = (User)session.getAttribute("user");
		SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
		
		try {
			folderId=Integer.parseInt(foldIdParam);
			nameSubFolder=request.getParameter("newSubFolder");
			dateCreation= new Date(Calendar.getInstance().getTime().getTime());
			
			isBadRequest= folderId==null || folderId < 0 || nameSubFolder==null || nameSubFolder.isEmpty();
			
		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
		}
		
		//extracting all the top Folders and the subFolders
		try{
			//extracting the entire tree of folders
			allTopFolders=folderDAO.findFoldersTreeByUser(user.getId());
			//extracting only the subfolders
			for(int i=0;i<allTopFolders.size();i++) {
				if(allTopFolders.get(i).getSubFolders()!=null && allTopFolders.get(i).getId()==folderId) {
					for(int j=0;j<allTopFolders.get(i).getSubFolders().size();j++) {
						allSubFolders.add(allTopFolders.get(i).getSubFolders().get(j));
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
				
		//check for the name
		for(SubFolder subFolder : allSubFolders) {
			
			if(subFolder.getName().equals(nameSubFolder)) {
				isNameOk=false;
				break;
			}
		}
		
		if(isBadRequest==false && isNameOk==true) {
			
			try {
				subFolderDAO.createSubFolder(nameSubFolder, dateCreation , folderId);
			} 
			catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to create subFolder!");
				return;
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
		}
		else if(isBadRequest==true){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values!");
			return;
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("SubFolder already present!");
			return;
		}
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
