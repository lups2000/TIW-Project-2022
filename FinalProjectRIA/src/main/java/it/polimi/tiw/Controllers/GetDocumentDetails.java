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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.Beans.Document;
import it.polimi.tiw.Beans.User;
import it.polimi.tiw.Dao.DocumentDAO;
import it.polimi.tiw.Utils.ConnectionHandler;


/**
 * Servlet implementation class GetDocumentDetails
 */
@WebServlet("/GetDocumentDetails")
public class GetDocumentDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    //constructor
    public GetDocumentDetails() {
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
		
		DocumentDAO documentDAO = new DocumentDAO(connection);
		Document document = new Document();
		Integer documentId=null;

		
		//catching the parameter-->documentId
		try{
			documentId=Integer.parseInt(request.getParameter("documentId"));
		}
		catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover document details!");
			return;
		}
		
		//check the existence of the document
		try {
			document=documentDAO.findDocumentById(documentId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not Possible to find Document!");
			return;
		}
		
		if(document == null || document.getId_owner() != user.getId()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("The document does not exists!");
	    	return;
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String jsonString = gson.toJson(document);
		
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
