package com.weebly.niseishun.watchthis.model;

/**
 * Entity representing a series with a title, a url of the series's page on its website, and a
 * counter representing the popularity of the series among relevant users.
 * 
 * @author Levi R
 *
 */
public class Entry implements Comparable<Entry> {

  private String title;
  private String url;
  private int counter;

  public Entry(String title, String url) {
    this.title = title;
    this.url = url;
    this.counter = 1;
  }

  public int compareTo(Entry e) {
    return Integer.compare(this.counter, e.counter);
  }

  public int getCounter() {
    return counter;
  }

  public void incrementCounter() {
    this.counter++;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

}
