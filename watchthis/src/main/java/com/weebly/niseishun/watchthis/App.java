package com.weebly.niseishun.watchthis;

import java.io.IOException;
import java.util.ArrayList;

import com.weebly.niseishun.watchthis.controllers.MALSearcher;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) {
    System.out.println("Hello World!");
    MALSearcher ms = new MALSearcher();
    try {
      ArrayList<User> users =
          ms.getLastUpdatedUsers("https://myanimelist.net/anime/5762/15_Bishoujo_Hyouryuuki/", 3);
      ArrayList<Entry> recs = ms.getRecommendedSeriesFromUsers(users, 2,
          "https://myanimelist.net/anime/5762/15_Bishoujo_Hyouryuuki/");
    } catch (IOException e) {
      System.out.println("IOE error!");
      e.printStackTrace();
    }

  }
}
