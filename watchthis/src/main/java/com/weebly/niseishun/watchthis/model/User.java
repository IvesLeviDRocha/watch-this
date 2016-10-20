package com.weebly.niseishun.watchthis.model;

public class User {
	
	private String username;
	private String listUrl;
	
	public User(String username, String listUrl) {
		super();
		this.username = username;
		this.listUrl = listUrl;
	}

	public String getListUrl() {
		return listUrl;
	}

	public String getUsername() {
		return username;
	}
	
}
