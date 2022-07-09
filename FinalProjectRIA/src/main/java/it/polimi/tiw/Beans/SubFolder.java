package it.polimi.tiw.Beans;

import java.sql.Date;
import java.util.List;

public class SubFolder {
	
	private int id;
	private String name;
	private Date date;
	private int id_folder;
	private List<Document> documents;
	
	public int getId() {
		return id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public int getId_folder() {
		return id_folder;
	}
	
	public String getName() {
		return name;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setId_folder(int id_folder) {
		this.id_folder = id_folder;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<Document> getDocuments() {
		return documents;
	}
	
	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
}
