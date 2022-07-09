package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/CreateSubFolder")
public class CreateSubFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine=null;
       
    //constructor
    public CreateSubFolder() {
        super();
    }
    
    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    	templateEngine=TemplateHandler.getEngine(servletContext, ".html");
    }

    private Date getMeYesterday() {
		return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
	}
    
    private Date getMeTomorrow() {
    	return new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doPost(request, response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ServletContext servletContext=getServletContext();
		Integer folderId=-1;
		String name=null;
		Date dateCreation=null;
		String path=null;
		String foldIdParam=request.getParameter("folderId");
		boolean isBadRequest=false;
		boolean isNameOk=true;
		FolderDAO folderDAO=new FolderDAO(connection);
		List<Folder> allTopFolders = new ArrayList<Folder>();
		List<SubFolder> allSubFolders = new ArrayList<SubFolder>();
		
		if (foldIdParam == null) {
			isBadRequest = true;
		}
		
		HttpSession session=request.getSession();
		if(session.isNew() || session.getAttribute("user")==null) {
			response.sendRedirect(servletContext.getContextPath());
			return;
		}
		
		User user = (User)session.getAttribute("user");
		SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
		
		try {
			folderId=Integer.parseInt(foldIdParam);
			name=request.getParameter("name");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			dateCreation = (Date) sdf.parse(request.getParameter("date"));
			isBadRequest= folderId==null || folderId < 0 || name==null || name.isEmpty() ||
					dateCreation==null || getMeYesterday().after(dateCreation) || getMeTomorrow().before(dateCreation);
			
		} 
		catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest = true;
		}
		
		//extracting all the top Folders and the subFolders
		try{
			//extracting the entire tree of folders
			allTopFolders=folderDAO.findFoldersTreeByUser(user.getId());
			//extracting only the subfolders
			for(int i=0;i<allTopFolders.size();i++) {
				if(allTopFolders.get(i).getSubFolders()!=null) {
					for(int j=0;j<allTopFolders.get(i).getSubFolders().size();j++) {
						allSubFolders.add(allTopFolders.get(i).getSubFolders().get(j));
					}
				}
			}
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to extract content!");
			return;
		}
		
		//check for the name
		for(SubFolder subFolder : allSubFolders) {
			if(subFolder.getName().equals(name)) {
				isNameOk=false;
				break;
			}
		}
		
		if(isBadRequest==false && isNameOk==true) {
			
			try {
				subFolderDAO.createSubFolder(name, dateCreation, folderId);
			} 
			catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to create subFolder!");
				return;
			}
			
			//now we must redirect the user to the Home Page
			path=servletContext.getContextPath()+"/HomePage";
			response.sendRedirect(path);
			
		}
		else if(isBadRequest==true){
			
			final WebContext ctx=new WebContext(request, response, servletContext,request.getLocale());
			path="/WEB-INF/CreateContent.html";
			ctx.setVariable("errorMsg2", "Missing/Incorrect Information");
			ctx.setVariable("allTopFolders",allTopFolders);
			ctx.setVariable("allSubFolders", allSubFolders);
			templateEngine.process(path, ctx,response.getWriter());
			return;
		}
		else {
			final WebContext ctx=new WebContext(request, response, servletContext,request.getLocale());
			path="/WEB-INF/CreateContent.html";
			ctx.setVariable("errorMsg2", "SubFolder already present!");
			ctx.setVariable("allTopFolders",allTopFolders);
			ctx.setVariable("allSubFolders", allSubFolders);
			templateEngine.process(path, ctx,response.getWriter());
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
