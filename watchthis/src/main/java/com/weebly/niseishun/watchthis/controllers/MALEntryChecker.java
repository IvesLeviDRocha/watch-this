package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;
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

  public MALEntryChecker(ArrayList<Entry> entries, Entry entry, StaffList staff) {
    this.entry = entry;
    this.staff = staff;
    this.entries = entries;
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
    // for each character
    List<Element> characterList = staffPage.selectElements(MALSearcher.characterStaffListSelector);
    characterList = characterList.subList(0, characterList.size() - 1);
    for (Element element : characterList) {
      Elements nameContainers = element.select(MALSearcher.vaNameSelector);
      if (nameContainers.toString().equals("")) {
        continue;
      }
      String va = nameContainers.first().html();
      if (staffMap.containsKey(va)) {
        entry.addToBonus(staffMap.get(va));
      }
    }
    // for each staff
    Elements staffList = staffPage.selectElements(MALSearcher.staffListSelector);
    for (Element element : staffList) {
      String name = element.select(MALSearcher.staffNameSelector).first().html();
      if (staffMap.containsKey(name)) {
        entry.addToBonus(staffMap.get(name));
      }
    }
  }

}
