package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;

import com.weebly.niseishun.watchthis.exception.PageUnavailableException;
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
    int numOfUsersToCheck = 50;
    float minimumPopularity = 0.1f;
    ArrayList<Entry> recommendations = new ArrayList<Entry>();
    switch (source) {
      case MAL:
        MALSearcher searcher;
        ArrayList<User> users;
        try {
          searcher = new MALSearcher(url);
          users = searcher.getLastUpdatedUsers(numOfUsersToCheck);
          recommendations = searcher.getRecommendedSeriesFromUsers(users, minimumPopularity);
        } catch (PageUnavailableException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        break;
      default:
        // TODO invalid source error
    }
    return recommendations;
  }

}
