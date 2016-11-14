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
  private float bonus;
  private float popularity;

  private static float popularityAdjustingFactor = 1f;
  private static float resultAdjustingFactor = 2f;

  public Entry(String title, String url) {
    this.title = title;
    this.url = url;
    this.counter = 1;
    this.bonus = 0;
  }

  public int compareTo(Entry e) {
    return Float.compare(this.getMatchValue(), e.getMatchValue());
  }

  public int getCounter() {
    return counter;
  }

  public void incrementCounter() {
    counter++;
  }

  public void calculatePopularity(int sampleSize) {
    popularity = counter * 1f / sampleSize;
  }

  public float getPopularity() {
    return popularity;
  }

  public void addToBonus(float valueToAdd) {
    bonus += valueToAdd;
  }

  public float getBonus() {
    return bonus;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public float getMatchValue() {
    return ((popularity * 100) * popularityAdjustingFactor + bonus) / resultAdjustingFactor;
  }

  public void bonusToLimit(int limit) {
    if (bonus > limit) {
      bonus = limit;
    }
  }

  public static void updatePopularityAdjustingFactor(float factor) {
    popularityAdjustingFactor = factor;
  }

  public static void updateResultAdjustingFactor(float factor) {
    resultAdjustingFactor = factor;
  }

  public String toString() {
    return Math.round(getMatchValue()) + " % | " + title;
  }

}
