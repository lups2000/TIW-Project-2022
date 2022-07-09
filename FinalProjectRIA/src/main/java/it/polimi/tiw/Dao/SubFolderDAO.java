package it.polimi.tiw.Dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import it.polimi.tiw.Beans.SubFolder;

public class SubFolderDAO {
	
	private Connection connection;
	
	//constructor
	public SubFolderDAO(Connection connection) {
		this.connection=connection;
	}

	public List<SubFolder> findSubFoldersByFolder(int folderId) throws SQLException{
		
		List<SubFolder> subFolders = new ArrayList<SubFolder>();
		String query = "SELECT * FROM dbprogetto.sotto_cartella WHERE id_cartella = ?";
		
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, folderId);
			
			try(ResultSet resultSet = preparedStatement.executeQuery();){
				
				while(resultSet.next()) {
					
					SubFolder subFolder = new SubFolder(); //creating a new SubFolder bean
					subFolder.setId(resultSet.getInt("idsottocartella"));
					subFolder.setId_folder(resultSet.getInt("id_cartella"));
					subFolder.setDate(resultSet.getDate("data_creazione"));
					subFolder.setName(resultSet.getString("nome"));
					
					subFolders.add(subFolder);
				}
			}
		}
		return subFolders;
	}
	
	public int findFatherFolderId(int subFolderId) throws SQLException {
		
		String query = "SELECT * FROM dbprogetto.sotto_cartella WHERE idsottocartella = ?";
		
		try(PreparedStatement preparedStatement= connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, subFolderId);
			
			try(ResultSet resultSet=preparedStatement.executeQuery()){
				
				if (!resultSet.isBeforeFirst()) //no results
					return -1;
				else {
					resultSet.next();
				    return resultSet.getInt("id_cartella");
				}
			}
		}
		
		
	}
	
	public SubFolder findSubFolderTree(int subFolderId) throws SQLException {
		//return the subfolder and it's documents
		DocumentDAO documentDAO= new DocumentDAO(connection);
		String query= "SELECT * FROM dbprogetto.sotto_cartella WHERE idsottocartella = ?";
		
		try(PreparedStatement preparedStatement= connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, subFolderId);
			
			try(ResultSet resultSet=preparedStatement.executeQuery()){
				
				if (!resultSet.isBeforeFirst()) //no results
					return null;
				else {
					resultSet.next();
					SubFolder subFolder = new SubFolder();
					subFolder.setId(resultSet.getInt("idsottocartella"));
					subFolder.setId_folder(resultSet.getInt("id_cartella"));
					subFolder.setDate(resultSet.getDate("data_creazione"));
					subFolder.setName(resultSet.getString("nome"));
				    subFolder.setDocuments(documentDAO.findDocumentsBySubFolder(subFolder.getId()));
				    return subFolder;
				}
			}
		}
	}
	
	public void createSubFolder(String name,Date dateCreation,int folderId) throws SQLException {
		
		String query="INSERT into dbprogetto.sotto_cartella (nome,data_creazione,id_cartella) VALUES (?,?,?)";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setString(1, name);
			preparedStatement.setDate(2,new java.sql.Date(dateCreation.getTime()));
			preparedStatement.setInt(3, folderId);
			
			preparedStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
	public void deleteSubFolder(int subFolderId) throws SQLException {
		
		String query = "DELETE FROM dbprogetto.sotto_cartella WHERE idsottocartella = ?";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, subFolderId);
			
			preparedStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
}