package it.polimi.tiw.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.Beans.Document;

public class DocumentDAO {
	
	private Connection connection;
	
	//constructor
	public DocumentDAO(Connection connection) {
		this.connection=connection;
	}
	
	public List<Document> findDocumentsBySubFolder(int subFolderId) throws SQLException{
		
		List<Document> documents=new ArrayList<Document>();
		String query="SELECT * FROM dbprogetto.documento WHERE id_sottocartella = ?";
		
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, subFolderId);
			
			try(ResultSet resultSet=preparedStatement.executeQuery();){
				
				while(resultSet.next()) {
					Document document=new Document(); //creating a new Document Bean
					document.setId(resultSet.getInt("iddocumento"));
					document.setDate(resultSet.getDate("data_creazione"));
					document.setName(resultSet.getString("nome"));
					document.setId_subfolder(resultSet.getInt("id_sottocartella"));
					document.setId_owner(resultSet.getInt("id_proprietario"));
					document.setSummary(resultSet.getString("sommario"));
					document.setType(resultSet.getString("tipo"));
					
					documents.add(document);
				}
			}
		}
		return documents;
	}
	
	public Document findDocumentById(int documentId) throws SQLException {
		
		String query="SELECT * FROM dbprogetto.documento WHERE iddocumento = ?";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setInt(1, documentId);
			
			try(ResultSet resultSet=preparedStatement.executeQuery();){
				
				if (!resultSet.isBeforeFirst()) //no results
					return null;
				else {
					resultSet.next();
					Document document = new Document();
					document.setId(resultSet.getInt("iddocumento"));
					document.setDate(resultSet.getDate("data_creazione"));
					document.setName(resultSet.getString("nome"));
					document.setId_owner(resultSet.getInt("id_proprietario"));
					document.setSummary(resultSet.getString("sommario"));
					document.setType(resultSet.getString("tipo"));
					document.setId_subfolder(resultSet.getInt("id_sottocartella"));
					
					return document;
				}
			}
		}
	}
	
	public void createDocument(String name,Date dateCreation,int subFolderId,int ownerId,String summary,String type) throws SQLException {
		
		String query="INSERT into dbprogetto.documento (nome,data_creazione,id_sottocartella,id_proprietario,sommario,tipo) VALUES (?,?,?,?,?,?)";
		
		try(PreparedStatement preparedStatement=connection.prepareStatement(query);){
			
			//mapping
			preparedStatement.setString(1,name);
			preparedStatement.setDate(2,new java.sql.Date(dateCreation.getTime()));
			preparedStatement.setInt(3, subFolderId);
			preparedStatement.setInt(4, ownerId);
			preparedStatement.setString(5, summary);
			preparedStatement.setString(6, type);
			
			preparedStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
	}
	
	public void changeDocumentPosition(int documentId,int nextSubFolderId) throws SQLException {
		
		String query="UPDATE dbprogetto.documento SET id_sottocartella = ? WHERE iddocumento= ?";
		try {
			PreparedStatement preparedStatement=connection.prepareStatement(query);
			//mapping
			preparedStatement.setInt(1, nextSubFolderId);
			preparedStatement.setInt(2, documentId);
			
			preparedStatement.executeUpdate();
			
		} 
		catch (SQLException e) {
			throw new SQLException("Error while accessing the DB");
		}
		
		
		
	}
}
