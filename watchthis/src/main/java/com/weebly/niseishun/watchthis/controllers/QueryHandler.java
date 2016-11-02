package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.util.ArrayList;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.Source;
import com.weebly.niseishun.watchthis.model.User;

/**
 * This handler performs the query algorithm to retrieve recommendations for a given input.
 * 
 * @author Levi Rocha
 *
 */
public class QueryHandler {

  /**
   * run algorithm to find recommendations for a given series
   * 
   * @param url to series page
   * @param source of series page
   * @return list of recommendations
   */
  public ArrayList<Entry> getRecommendationsWithURL(String url, Source source) {
    int numOfUsersToCheck = 100;
    int minimumPopularity = 10;
    ArrayList<Entry> recommendations = new ArrayList<Entry>();
    switch (source) {
      case MAL:
        MALSearcher searcher = new MALSearcher();
        ArrayList<User> users;
        try {
          users = searcher.getLastUpdatedUsers(url, numOfUsersToCheck);
          recommendations = searcher.getRecommendedSeriesFromUsers(users, minimumPopularity, url);
        } catch (IOException e) {
          System.out.println("ioe in QH");
          e.printStackTrace();
        }
        break;
      default:
        // TODO invalid source error
    }
    return recommendations;
  }

}
