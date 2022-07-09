package it.polimi.tiw.Dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.Beans.Document;
import it.polimi.tiw.Beans.Folder;

public class FolderDAO {
	
	private Connection connection;
	
	//constructor
	public FolderDAO(Connection connection) {
		this.connection=connection;
	}
	
	public List<Folder> findFoldersTreeByUser(int userId) throws SQLException{
		
		List<Folder> folders = new ArrayList<Folder>();
		SubFolderDAO subFolderDAO = new SubFolderDAO(connection);
		String query = "SELECT * FROM dbprogetto.cartella WHERE id_proprietario = ?";
		
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);){
			//mapping
			preparedStatement.setInt(1, userId);
			
			try(ResultSet resultSet = preparedStatement.executeQuery();){
				
				while(resultSet.next()) {
					
					Folder folder = new Folder(); //creating a Folder bean
					folder.setId(resultSet.getInt("idcartella"));
					folder.setId_owner(resultSet.getInt("id_proprietario"));
					folder.setName(resultSet.getString("nome"));
					folder.setDate(resultSet.getDate("data_creazione"));
					folder.setSubFolders(subFolderDAO.findSubFoldersByFolder(folder.getId()));
	
					folders.add(folder);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return folders;
		
	}
	
	public Folder findFolderById(int folderId) throws SQLException {
		
		String query = "SELECT * FROM dbprogetto.cartella WHERE idcartella = ?";
		
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);){
			//mapping
			preparedStatement.setInt(1, folderId);
			
			try(ResultSet resultSet = preparedStatement.executeQuery();){
				
				if (!resultSet.isBeforeFirst()) { //no results
					return null;
				}
				else {
					resultSet.next();
					Folder folder = new Folder(); //creating a Folder bean
					folder.setId(resultSet.getInt("idcartella"));
					folder.setId_owner(resultSet.getInt("id_proprietario"));
					folder.setName(resultSet.getString("nome"));
					folder.setDate(resultSet.getDate("data_creazione"));
					
					return folder;
				}
			}
		}
	}
	
	public List<Folder> findAllTopFolders(int userId) throws SQLException {
		
		List<Folder> folders = new ArrayList<Folder>();
		String query = "SELECT * FROM dbprogetto.cartella WHERE id_proprietario = ?";
		
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);){
			//mapping
			preparedStatement.setInt(1, userId);
			
			try(ResultSet resultSet = preparedStatement.executeQuery();){
				
				while(resultSet.next()) {
					
					Folder folder = new Folder(); //creating a Folder bean
					folder.setId(resultSet.getInt("idcartella"));
					folder.setId_owner(resultSet.getInt("id_proprietario"));
					folder.setName(resultSet.getString("nome"));
					folder.setDate(resultSet.getDate("data_creazione"));
					
					folders.add(folder);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return folders;
	}
	
	public void createFolder(String name,Date dateCreation,int userId) throws SQLException {
		
		String query="INSERT into dbprogetto.cartella (nome,data_creazione,id_proprietario) VALUES (?,?,?)";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setString(1, name);
			preparedStatement.setDate(2,new java.sql.Date(dateCreation.getTime()));
			preparedStatement.setInt(3, userId);
			
			preparedStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
	public void deleteFolder(int folderId) throws SQLException {
		
		String query = "DELETE FROM dbprogetto.cartella WHERE idcartella = ?";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, folderId);
			
			preparedStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}

}

