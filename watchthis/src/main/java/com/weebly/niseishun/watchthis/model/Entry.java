package com.weebly.niseishun.watchthis.model;

public class Entry {
	
	private String title;
	private String url;
	private int counter;
	
	public Entry(String title, String url) {
		this.title = title;
		this.url = url;
		this.counter = 1;
	}
	
	public int getCounter() {
		return counter;
	}
	
	public void setCounter(int counter) {
		this.counter = counter;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

}
