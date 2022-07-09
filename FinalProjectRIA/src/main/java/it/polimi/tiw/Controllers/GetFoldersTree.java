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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.Beans.Folder;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.FolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


@WebServlet("/GetFoldersTree")
public class GetFoldersTree extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    //constructor
    public GetFoldersTree() {
        super();
    }
    
    public void init() throws UnavailableException {
    	ServletContext servletContext=getServletContext();
    	connection=ConnectionHandler.getConnection(servletContext);
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		
		if(session.isNew() || user == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		FolderDAO folderDAO = new FolderDAO(connection);
		List<Folder> foldersTree = new ArrayList<Folder>();
		
		try {
			foldersTree = folderDAO.findFoldersTreeByUser(user.getId());
		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover folders");
			return;
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String jsonString = gson.toJson(foldersTree);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonString);//serialize the folders's tree
		
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
