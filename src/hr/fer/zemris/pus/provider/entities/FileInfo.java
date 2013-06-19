package hr.fer.zemris.pus.provider.entities;

import java.io.Serializable;
import java.rmi.server.UID;

public class FileInfo implements Serializable{
	
	private static final long serialVersionUID = 6759425377115924022L;
	
	private String id;
	private String owner;
	private String details;
	private String name;
	
	public FileInfo(String owner, String details, String name) {
		this.id = new UID().toString();
		this.owner = owner;
		this.details = details;
		this.name = name;
	}
	
	public FileInfo(String uid, String owner, String details, String name) {
		this.id = uid;
		this.owner = owner;
		this.details = details;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
