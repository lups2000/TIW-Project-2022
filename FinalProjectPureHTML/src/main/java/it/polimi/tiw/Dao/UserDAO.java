package it.polimi.tiw.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.polimi.tiw.Beans.User;


public class UserDAO {
	
	private Connection connection;
	
	//constructor
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	//method to check if the credentials are okay or not
	public User checkCredentials(String user_name,String psw) throws SQLException {
		
		String query= "SELECT idutente, username, nome, cognome FROM dbprogetto.utente WHERE username = ? AND password = ?";
		//the query must be prepared in order to avoid problems such as the SQL injection
		try(PreparedStatement preparedStatement=connection.prepareStatement(query)){
			//mapping
			preparedStatement.setString(1, user_name);
			preparedStatement.setString(2, psw);
			
			try(ResultSet result = preparedStatement.executeQuery();){
				if (!result.isBeforeFirst()) // results are finished
					return null;
				else {
					result.next(); //pointer
					
					User user = new User(); //create a new User to save the data
					user.setId(result.getInt("idutente"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("nome"));
					user.setSurname(result.getString("cognome"));
					
					return user;
				}
			}
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
	//method to get a user from his id
	public User getUserByUsername(String username) throws SQLException {
		
		//string prepared
		String query="SELECT * FROM dbprogetto.utente WHERE username = ?";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query)){
			preparedStatement.setString(1, username);
			
			try(ResultSet resultSet=preparedStatement.executeQuery()){
				if (!resultSet.isBeforeFirst()) // results are finished
					return null;
				else {
					resultSet.next();
					
					User user=new User();
					user.setId(resultSet.getInt("idutente"));
					user.setUsername(resultSet.getString("username"));
					user.setName(resultSet.getString("nome"));
					user.setSurname(resultSet.getString("cognome"));
					
					return user;
				}
			}
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
	//method to get a user from his id
	public User getUserById(int id) throws SQLException {
			
			//string prepared
			String query="SELECT * FROM dbprogetto.utente WHERE idutente = ?";
			
			try(PreparedStatement preparedStatement=connection.prepareStatement(query)){
				preparedStatement.setInt(1, id);
				
				try(ResultSet resultSet=preparedStatement.executeQuery()){
					if (!resultSet.isBeforeFirst()) // results are finished
						return null;
					else {
						resultSet.next();
						
						User user=new User();
						user.setId(resultSet.getInt("idutente"));
						user.setUsername(resultSet.getString("username"));
						user.setName(resultSet.getString("nome"));
						user.setSurname(resultSet.getString("cognome"));
						
						return user;
				}
			}
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
	public void registerUser(String name,String surname,String username,String password) throws SQLException {
		
		//query prepared
		String query="INSERT INTO dbprogetto.utente (username,password,nome,cognome) VALUES(?,?,?,?)";
		try(PreparedStatement preparedStatement=connection.prepareStatement(query)){
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			preparedStatement.setString(3, name);
			preparedStatement.setString(4, surname);
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}

}
