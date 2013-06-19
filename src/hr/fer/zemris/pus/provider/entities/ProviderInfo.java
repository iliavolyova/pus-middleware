package hr.fer.zemris.pus.provider.entities;

import java.io.Serializable;

public class ProviderInfo implements Serializable{
	
	private static final long serialVersionUID = -3250029613672336650L;
	
	private String id;
	private String name;
	private String adress;
	
	public ProviderInfo(String id, String name, String adress) {
		super();
		this.id = id;
		this.name = name;
		this.adress = adress;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAdress() {
		return adress;
	}
	public void setAdress(String adress) {
		this.adress = adress;
	}
	
}
