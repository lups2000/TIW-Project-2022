package it.polimi.tiw.Utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

//this class manages the connection with the DB
//2 methods: -getConnection(servletContext)
//           -closeConnection(connection)
public class ConnectionHandler {

	
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection connection = null;
		try {
			//mapping 
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			//create a new object Driver
			Class.forName(driver);
			//set the connection
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Impossible to load the database driver!");
		} catch (SQLException e) {
			throw new UnavailableException("Imbossible to get the db connection");
		}
		return connection;
	}

	public static void closeConnection(Connection connection) throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}
	
}
