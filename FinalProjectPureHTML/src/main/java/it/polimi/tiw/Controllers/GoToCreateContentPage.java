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


import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.SubFolder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;
import it.polimi.tiw.Utils.TemplateHandler;


@WebServlet("/ContentManagementPage")
public class GoToCreateContentPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine=null;
	
	public void init() throws UnavailableException {
		ServletContext servletContext=getServletContext();
		connection=ConnectionHandler.getConnection(servletContext);
		templateEngine=TemplateHandler.getEngine(servletContext, ".html");
	}
       
    //constructor
    public GoToCreateContentPage() {
        super();
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		List<Folder> allTopFolders = new ArrayList<Folder>();
		List<SubFolder> allSubFolders=new ArrayList<SubFolder>();
		FolderDAO folderDAO= new FolderDAO(connection);

		HttpSession session=request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath()); //redirect to login page
			return;
		}
	
		User user = (User)session.getAttribute("user");

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
		
	
		String path="/WEB-INF/CreateContent.html";
		ServletContext servletContext=getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
		ctx.setVariable("user", user);
		ctx.setVariable("allTopFolders", allTopFolders);
		ctx.setVariable("allSubFolders", allSubFolders);
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
