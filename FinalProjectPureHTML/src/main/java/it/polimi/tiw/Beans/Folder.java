package it.polimi.tiw.Beans;

import java.sql.Date;
import java.util.List;

public class Folder {
	
	private int id;
	private String name;
	private Date date;
	private int id_owner;
	private List<SubFolder> subFolders;
	
	public int getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}
	
	public int getId_owner() {
		return id_owner;
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
	
	public void setId_owner(int id_owner) {
		this.id_owner = id_owner;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<SubFolder> getSubFolders() {
		return subFolders;
	}
	
	public void setSubFolders(List<SubFolder> subFolders) {
		this.subFolders = subFolders;
	}
	
}

