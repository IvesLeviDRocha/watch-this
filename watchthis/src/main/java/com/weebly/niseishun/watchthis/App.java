package com.weebly.niseishun.watchthis;

import java.io.IOException;
import java.util.ArrayList;

import com.weebly.niseishun.watchthis.controllers.MALSearcher;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;


public class App {
  public static void main(String[] args) {
    MALSearcher ms = new MALSearcher();
    String url = "https://myanimelist.net/anime/15809/Hataraku_Maou-sama";
    try {
      ArrayList<User> users = ms.getLastUpdatedUsers(url, 500);
      ArrayList<Entry> recs = ms.getRecommendedSeriesFromUsers(users, 10, url);
      for (Entry entry : recs) {
        System.out.println("Series: " + entry.getTitle() + " || Liked by: " + entry.getCounter());
      }
    } catch (IOException e) {
      // ioe error
    }

  }
}
