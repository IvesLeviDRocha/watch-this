package com.weebly.niseishun.watchthis.model;

import java.util.concurrent.ConcurrentHashMap;

public class StaffList {

  private ConcurrentHashMap<String, Float> staff;

  public StaffList() {
    staff = new ConcurrentHashMap<String, Float>();
  }

  public void addToList(String name, float value) {
    if (staff.containsKey(name)) {
      staff.put(name, staff.get(name) + value);
    } else {
      staff.put(name, value);
    }
  }

  public ConcurrentHashMap<String, Float> getList() {
    return staff;
  }

}
