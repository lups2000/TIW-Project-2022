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

import it.polimi.tiw.Utils.TemplateHandler;
import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;

/**
 * This servlet handles the information contained in the HomePage.
 *
 */
@WebServlet("/HomePage")
public class GoToHomePage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    //constructor
    public GoToHomePage() {
        super();
    }
    
    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    	templateEngine=TemplateHandler.getEngine(servletContext, ".html");
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		if (session.isNew() || user == null) {
			response.sendRedirect(getServletContext().getContextPath()); //redirect to login page
			return;
		}
		
		FolderDAO folderDAO = new FolderDAO(connection);
		List<Folder> foldersTree = new ArrayList<Folder>();
		
		try {
			foldersTree = folderDAO.findFoldersTreeByUser(user.getId());
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover folders!");
			return;
		}
		
		//now redirect to the HomePage
		String path="/WEB-INF/Home.html";
		ServletContext servletContext=getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext,request.getLocale());
		ctx.setVariable("foldersTree", foldersTree);
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
