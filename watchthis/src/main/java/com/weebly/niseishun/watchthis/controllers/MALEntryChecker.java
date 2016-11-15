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
      // update entry's url to standard
      entry.setUrl(MALSearcher.absoluteUrl(entry.getUrl()));
      // calculate bonus
      calculateBonus();
      // add to results list
      entries.add(entry);
    } catch (PageUnavailableException e) {
      // could not check this entry
    }
  }

  private void calculateBonus() throws PageUnavailableException {
    // get map containing relevant staff
    ConcurrentHashMap<String, Float> staffMap = staff.getList();
    // get html content from entry's page
    PageScrapper staffPage = PageScrapper.fromUrl(entry.getUrl() + MALSearcher.staffSuffix);
    // update bonus for each criteria
    updateBonusForVAs(staffMap, staffPage);
    updateBonusForGenres(staffPage);
    updateBonusForStaff(staffMap, staffPage);
    updateBonusForUserRecs();
  }

  private void updateBonusForUserRecs() {
    // check if user rec
    if (userRecs.containsKey(entry.getTitle())) {
      float value = userRecs.get(entry.getTitle());
      entry.addToBonus(value);
    }
  }

  private void updateBonusForStaff(ConcurrentHashMap<String, Float> staffMap,
      PageScrapper staffPage) {
    // for each staff
    Elements staffList = staffPage.selectElements(MALSearcher.staffListSelector);
    float value = 0f;
    for (Element element : staffList) {
      String name = element.select(MALSearcher.staffNameSelector).first().html();
      if (staffMap.containsKey(name)) {
        value += staffMap.get(name);
      }
    }
    limitValueAndAddToBonus(value, 100);
  }

  private void limitValueAndAddToBonus(float value, int limit) {
    if (value > limit) {
      entry.addToBonus(limit);
    } else {
      entry.addToBonus(value);
    }
  }

  private void updateBonusForGenres(PageScrapper staffPage) {
    // for each genre
    float value = 0f;
    Elements genreList = staffPage.selectElements(MALSearcher.genreSelector);
    for (Element genre : genreList) {
      String genreTitle = genre.html();
      if (genres.containsKey(genreTitle)) {
        value += genres.get(genreTitle);
      }
    }
    limitValueAndAddToBonus(value, 30);
  }

  private void updateBonusForVAs(ConcurrentHashMap<String, Float> staffMap,
      PageScrapper staffPage) {
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
    limitValueAndAddToBonus(value, 10);
  }

}
