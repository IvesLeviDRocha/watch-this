package com.weebly.niseishun.watchthis.model;

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
