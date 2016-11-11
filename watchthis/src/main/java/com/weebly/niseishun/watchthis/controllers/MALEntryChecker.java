package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.weebly.niseishun.watchthis.exception.PageUnavailableException;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.StaffList;

public class MALEntryChecker implements Runnable {

  private Entry entry;
  private StaffList staff;
  private ArrayList<Entry> entries;
  private HashMap<String, Float> genres;
  private HashMap<String, Float> userRecs;

  public MALEntryChecker(ArrayList<Entry> entries, Entry entry, StaffList staff,
      HashMap<String, Float> genres, HashMap<String, Float> userRecs) {
    this.entry = entry;
    this.staff = staff;
    this.entries = entries;
    this.genres = genres;
    this.userRecs = userRecs;
  }

  public void run() {
    try {
      // update url
      entry.setUrl(MALSearcher.absoluteUrl(entry.getUrl()));
      // calculate bonus
      calculateBonus(entry, staff);
      // add to list
      entries.add(entry);
    } catch (PageUnavailableException e) {
      System.out.println("error in checker thread");
      e.printStackTrace();
    }
  }

  private void calculateBonus(Entry entry, StaffList staff) throws PageUnavailableException {
    ConcurrentHashMap<String, Float> staffMap = staff.getList();
    PageScrapper staffPage = PageScrapper.fromUrl(entry.getUrl() + MALSearcher.staffSuffix);
    // for each va
    List<Element> characterList = staffPage.selectElements(MALSearcher.characterStaffListSelector);
    characterList = characterList.subList(0, characterList.size() - 1);
    float value = 0f;
    for (Element element : characterList) {
      Elements nameContainers = element.select(MALSearcher.vaNameSelector);
      if (nameContainers.toString().equals("")) {
        continue;
      }
      String va = nameContainers.first().html();
      if (staffMap.containsKey(va)) {
        value += staffMap.get(va);
      }
    }
    if (value > 10) {
      entry.addToBonus(10);
    } else {
      entry.addToBonus(value);
    }
    // for each genre
    value = 0f;
    Elements genreList = staffPage.selectElements(MALSearcher.genreSelector);
    for (Element genre : genreList) {
      String genreTitle = genre.html();
      if (genres.containsKey(genreTitle)) {
        value += genres.get(genreTitle);
      }
    }
    if (value > 30) {
      entry.addToBonus(30);
    } else {
      entry.addToBonus(value);
    }
    // for each staff
    Elements staffList = staffPage.selectElements(MALSearcher.staffListSelector);
    value = 0f;
    for (Element element : staffList) {
      String name = element.select(MALSearcher.staffNameSelector).first().html();
      if (staffMap.containsKey(name)) {
        value += staffMap.get(name);
      }
    }
    if (value > 100) {
      entry.addToBonus(100);
    } else {
      entry.addToBonus(value);
    }
    // check if user rec
    if (userRecs.containsKey(entry.getTitle())) {
      value = userRecs.get(entry.getTitle());
      entry.addToBonus(value);
    }
  }

}
