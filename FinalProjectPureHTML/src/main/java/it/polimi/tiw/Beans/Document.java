package it.polimi.tiw.Beans;

import java.sql.Date;

public class Document {
	
	private int id;
	private String name;
	private Date date;
	private String summary;
	private String type;
	private int id_subfolder;
	private int id_owner;
	
	public int getId() {
		return id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public int getId_owner() {
		return id_owner;
	}
	
	public int getId_subfolder() {
		return id_subfolder;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getType() {
		return type;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setId_owner(int id_owner) {
		this.id_owner = id_owner;
	}
	
	public void setId_subfolder(int id_subfolder) {
		this.id_subfolder = id_subfolder;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public void setType(String type) {
		this.type = type;
	}

}
