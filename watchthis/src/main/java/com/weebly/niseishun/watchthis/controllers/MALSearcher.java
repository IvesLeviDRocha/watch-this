package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

public class MALSearcher {

  private String userSelector = "table.table-recently-updated > tbody > tr:not(:first-child)";
  private String nameSelector = "td.borderClass.di-t.w100:first-child > div.di-tc.va-m.al.pl4 > a";
  private String listUrlPrefix = "https://myanimelist.net/profile/";
  private String statusSelector = "td:nth-child(3)";
  private String statsSuffix = "/stats?m=all&show=";
  private int pageElementsIncrement = 75;

  /**
   * get last users to update a series on MAL
   * 
   * @param url of series
   * @param numOfUsers to be returned
   * @return list of users
   * @throws IOException
   */
  public ArrayList<User> getLastUpdatedUsers(String url, int numOfUsers) throws IOException {
    ArrayList<User> users = new ArrayList<User>();
    int page = 0;
    while (users.size() < numOfUsers) {
      PageScrapper seriesPage =
          PageScrapper.fromUrl(url + statsSuffix + String.valueOf(page * pageElementsIncrement));
      Elements lines = seriesPage.selectElements(userSelector);
      for (Element element : lines) {
        if (users.size() >= numOfUsers) {
          break;
        }
        String status = element.select(statusSelector).first().html();
        if (status.equals("Completed")) {
          String username = element.select(nameSelector).first().html();
          String listUrl = listUrlPrefix + username;
          User user = new User(username, listUrl);
          users.add(user);
        }
      }
      page++;
    }
    return users;
  }

  /**
   * get the most liked series by given users
   * 
   * @param users whose lists should be checked
   * @param minLikes minimum number of users to have liked a series for it to be recommended
   * @return list of recommended series
   */
  public ArrayList<Entry> getRecommendedSeriesFromUsers(ArrayList<User> users, int minLikes) {
    ArrayList<Entry> entries = new ArrayList<Entry>();
    // TODO
    return entries;
  }

}
