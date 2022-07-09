package it.polimi.tiw.Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.sql.Date;


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
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Dao.SubFolderDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


@WebServlet("/CreateDocument")
@MultipartConfig
public class CreateDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Connection connection;
       
    //constructor
    public CreateDocument() {
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
		
		Integer subFolderId=-1;
		String name=null;
		String summary=null;
		String type=null;
		Date dateCreation=null;
		boolean isBadRequest=false;
		boolean isNameOk=true;
		
		String subFoldIdParam=request.getParameter("subFolderId");
		
		DocumentDAO documentDAO=new DocumentDAO(connection);
		SubFolderDAO subFolderDAO=new SubFolderDAO(connection);
		
		if(subFoldIdParam==null) {
			isBadRequest=true;
		}
		
		HttpSession session=request.getSession();
		if(session.isNew() || session.getAttribute("user")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("session ended");
			return;
		}
		
		User user=(User)session.getAttribute("user");
		
		try {
			
			subFolderId=Integer.parseInt(subFoldIdParam);
			name=request.getParameter("nameDoc");
			summary=request.getParameter("summaryDoc");
			type=request.getParameter("typeDoc");
			dateCreation= new Date(Calendar.getInstance().getTime().getTime());
			
			isBadRequest= subFolderId<0 || name==null || name.isEmpty() ||
					summary==null || summary.isEmpty() || type==null || type.isEmpty();

		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest=true;
		}
				
		//check the name
		try {
			for(Document document : subFolderDAO.findSubFolderTree(subFolderId).getDocuments()) {
				if(document.getName().equals(name) && document.getId_subfolder()==subFolderId) {
					isNameOk=false;
					break;
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if(isBadRequest==false && isNameOk==true) {
			
			try {
				documentDAO.createDocument(name, dateCreation, subFolderId, user.getId(), summary, type);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to create document!");
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
			response.getWriter().println("Document already present!");
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
