package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


@WebServlet("/CreateDocument")
public class CreateDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Connection connection;
	TemplateEngine templateEngine=null;
       
    //constructor
    public CreateDocument() {
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
		Integer subFolderId=-1;
		String name=null;
		String summary=null;
		String type=null;
		Date dateCreation=null;
		boolean isBadRequest=false;
		boolean isNameOk=true;
		String path=null;
		String subFoldIdParam=request.getParameter("subFolderId");
		
		DocumentDAO documentDAO=new DocumentDAO(connection);
		FolderDAO folderDAO=new FolderDAO(connection);
		SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
		List<Folder> allTopFolders = new ArrayList<Folder>();
		List<SubFolder> allSubFolders = new ArrayList<SubFolder>();
		
		if(subFoldIdParam==null) {
			isBadRequest=true;
		}
		
		HttpSession session=request.getSession();
		if(session.isNew() || session.getAttribute("user")==null) {
			response.sendRedirect(servletContext.getContextPath());
			return;
		}
		
		User user=(User)session.getAttribute("user");
		
		try {
			
			subFolderId=Integer.parseInt(subFoldIdParam);
			name=request.getParameter("name");
			summary=request.getParameter("summary");
			type=request.getParameter("typeDoc");
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateCreation=(Date)simpleDateFormat.parse(request.getParameter("date"));
			
			isBadRequest= subFolderId<0 || name==null || name.isEmpty() ||
					summary==null || summary.isEmpty() || type==null || type.isEmpty() ||
					dateCreation==null || getMeYesterday().after(dateCreation) || getMeTomorrow().before(dateCreation);
			
			
		} catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest=true;
		}
				
		//check the name
		try {
			for(Document document : subFolderDAO.findSubFolderTree(subFolderId).getDocuments()) {
				if(document.getName().equals(name)) {
					isNameOk=false;
					break;
				}
			}
		} catch (SQLException e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to extract content!");
			return;
		}
		
		if(isBadRequest==false && isNameOk==true) {
			
			try {
				documentDAO.createDocument(name, dateCreation, subFolderId, user.getId(), summary, type);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to create Document!");
				return;
			}
			
			//now redirect to Documents Page
			path=getServletContext().getContextPath()+"/DocumentsPage?subFolderId="+subFoldIdParam;
			response.sendRedirect(path);
			
		}
		else {
			
			//extracting all the top Folders and Subfolders
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
			
			if(isBadRequest==true) {
				final WebContext ctx = new WebContext(request, response, servletContext);
				path="/WEB-INF/CreateContent.html";
				ctx.setVariable("errorMsg3", "Missing/Incorrect Information");
				ctx.setVariable("allTopFolders", allTopFolders);
				ctx.setVariable("allSubFolders", allSubFolders);
				templateEngine.process(path, ctx,response.getWriter());
				return;
			}
			else if(isNameOk==false) {
				final WebContext ctx = new WebContext(request, response, servletContext);
				path="/WEB-INF/CreateContent.html";
				ctx.setVariable("errorMsg3", "Document already present!");
				ctx.setVariable("allTopFolders", allTopFolders);
				ctx.setVariable("allSubFolders", allSubFolders);
				templateEngine.process(path, ctx,response.getWriter());
				return;
			}
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
