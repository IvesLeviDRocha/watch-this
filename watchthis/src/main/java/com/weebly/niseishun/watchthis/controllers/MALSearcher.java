package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.weebly.niseishun.watchthis.exception.PageUnavailableException;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.StaffList;
import com.weebly.niseishun.watchthis.model.User;

/**
 * This searcher implements the functionalities pertaining to the source MyAnimeList.
 * 
 * @author Levi Rocha
 *
 */
public class MALSearcher {

  public static final float mainVAValue = 2f;
  public static final float otherVAValue = 0.5f;
  public static final float directorValue = 28f;
  public static final float creatorValue = 36f;
  public static final float musicValue = 18f;
  public static final float scriptValue = 18f;
  public static final float animationDirectorValue = 12f;
  public static final float seriesCompositionValue = 18f;
  public static final float characterDesignValue = 12f;
  public static final float genreValue = 16f;
  public static final float genreValueDecline = 0f;

  public static final String userSelector =
      "table.table-recently-updated > tbody > tr:not(:first-child)";
  public static final String nameSelector =
      "td.borderClass.di-t.w100:first-child > div.di-tc.va-m.al.pl4 > a";
  public static final String listUrlPrefix = "https://myanimelist.net/animelist/";
  public static final String listUrlSuffix = "?status=2";
  public static final String statsSuffix = "/stats?m=all&show=";
  public static final int pageElementsIncrement = 75;
  public static final String titleSelector = "#contentWrapper > div:nth-child(1) > h1 > span";
  public static final String scoreSelectorFromStats = "td:nth-child(2)";
  public static final String detailsSelector = "#horiznav_nav > ul > li:nth-child(1) > a";
  public static final String staffSuffix = "/characters";
  public static final String characterStaffListSelector =
      "#content > table > tbody > tr > td:nth-child(2) > div.js-scrollfix-bottom-rel > table";
  public static final String characterRoleSelector = "tbody > tr > td:nth-child(2) > div > small";
  public static final String vaNameSelector =
      "tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(1) > td:nth-child(1) > a";
  public static final String staffListSelector =
      "#content > table > tbody > tr > td:nth-child(2) > div.js-scrollfix-bottom-rel > table:last-child > tbody > tr";
  public static final String staffNameSelector = "td:nth-child(2) > a";
  public static final String staffPositionSelector = "td:nth-child(2) > small";
  public static final String categoryTotalsSelector =
      ":contains(Mean Score:), td.category_totals:contains(Mean Score:)";
  public static final String seriesPagePrefix = "https://myanimelist.net/anime/";
  public static final String malAPIurlPrefix = "https://myanimelist.net/malappinfo.php?u=";
  public static final String malAPIurlSufix = "&status=all&type=anime.";
  public static final String genreSelector =
      "#content > table > tbody > tr > td.borderClass > div > div:contains(Genre) > a";


  private String url;
  private HashMap<String, Float> staffPositions;
  private HashMap<String, Float> genres;

  public MALSearcher(String url) throws PageUnavailableException {
    this.url = absoluteUrl(url);
    staffPositions = new HashMap<String, Float>();
    staffPositions.put("Original Creator", creatorValue);
    staffPositions.put("Director", directorValue);
    staffPositions.put("Music", musicValue);
    staffPositions.put("Series Composition", seriesCompositionValue);
    staffPositions.put("Animation Director", animationDirectorValue);
    staffPositions.put("Character Design", characterDesignValue);
    staffPositions.put("Script", scriptValue);
    staffPositions.put("Original Character Design", characterDesignValue);
    staffPositions.put("Chief Animation Director", animationDirectorValue);
    genres = new HashMap<String, Float>();


    System.out.println("mal searcher created for " + this.url);
  }

  /**
   * get last users to update a series on MAL
   * 
   * @param url of series
   * @param numOfUsers to be returned
   * @return list of users with specified size
   * @throws PageUnavailableException
   * @throws IOException
   */
  public ArrayList<User> getLastUpdatedUsers(int numOfUsers) throws PageUnavailableException {
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
        String score = element.select(scoreSelectorFromStats).first().html();
        if (!score.equals("-")) {
          String username = element.select(nameSelector).first().html();
          String listUrl = listUrlPrefix + username + listUrlSuffix;
          User user = new User(username, listUrl);
          users.add(user);
        }
      }
      page++;
    }
    return users;
  }

  /**
   * get the most liked series by users who also liked given series
   * 
   * @param users whose lists should be checked
   * @param minLikes minimum number of users to have liked a series for it to be recommended
   * @param seriesUrl name of series the user input
   * @return list of recommended series
   * @throws PageUnavailableException
   * @throws IOException
   */
  public ArrayList<Entry> getRecommendedSeriesFromUsers(ArrayList<User> users, float minPopularity)
      throws PageUnavailableException {
    ConcurrentHashMap<String, Entry> entriesMap = new ConcurrentHashMap<String, Entry>();
    // get series data
    String staffUrl = url + staffSuffix;
    PageScrapper seriesPage = PageScrapper.fromUrl(staffUrl);
    String seriesName = seriesPage.selectFirstElement(titleSelector).html();
    System.out.println(seriesName);
    // get staff
    StaffList staff = new StaffList();
    // for each character
    List<Element> characterList = seriesPage.selectElements(characterStaffListSelector);
    characterList = characterList.subList(0, characterList.size() - 1);
    for (Element element : characterList) {
      Element nameContainer = element.select(MALSearcher.vaNameSelector).first();
      if (nameContainer == null) {
        continue;
      }
      String va = nameContainer.html();
      String role = element.select(characterRoleSelector).first().html();
      if (role.equals("Main")) {
        staff.addToList(va, mainVAValue);
      } else {
        staff.addToList(va, otherVAValue);
      }
    }
    // for each staff
    Elements staffList = seriesPage.selectElements(staffListSelector);
    for (Element element : staffList) {
      Element staffPosition = element.select(staffPositionSelector).first();
      if (staffPosition == null) {
        continue;
      }
      String[] roles = staffPosition.html().split(", ");
      String name = element.select(staffNameSelector).first().html();
      for (String role : roles) {
        if (staffPositions.containsKey(role)) {
          staff.addToList(name, staffPositions.get(role));
        }
      }
    }
    System.out.println("done staff list");
    // for each genre
    Elements genreList = seriesPage.selectElements(genreSelector);
    int dec = 0;
    for (Element genre : genreList) {
      if (genre == null) {
        continue;
      }
      genres.put(genre.html(), genreValue - dec * genreValueDecline);
      dec++;
    }

    ArrayList<Thread> retrievers = new ArrayList<Thread>();
    for (int i = 0; i < users.size(); i++) {
      User user = users.get(i);
      Thread retriever = new Thread(new MALListRetriever(user, entriesMap, seriesName));
      retrievers.add(retriever);
      retriever.start();
    }
    for (Thread retriever : retrievers) {
      try {
        retriever.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    /*
     * // create threads to check users list. max thread count = simultaneousThreads for (int i = 0;
     * i < users.size(); i += simultaneousThreads) { ArrayList<Thread> retrievers = new
     * ArrayList<Thread>(); // create the threads according to limit for (int j = i; j < i +
     * simultaneousThreads; j++) { if (j < users.size()) { User user = users.get(j); Thread
     * retriever = new Thread(new MALListRetriever(user, entriesMap, seriesName));
     * retrievers.add(retriever); retriever.start(); } } // wait for threads before creating again
     * for (Thread retriever : retrievers) { try { retriever.join(); } catch (InterruptedException
     * e) { e.printStackTrace(); } } System.out.println("done one thread loop"); }
     */

    System.out.println("done getting entries");
    // list of entries to be returned
    ArrayList<Entry> entries = new ArrayList<Entry>();

    // confirm total of relevant users and remove input series from list
    int initialSampleSize = users.size();
    int counterForInputSeries = entriesMap.get(seriesName).getCounter();
    System.out.println("relevant users checked: " + counterForInputSeries);
    entriesMap.remove(seriesName);
    float popularityAdjustingFactor = counterForInputSeries * 1f / initialSampleSize;
    Entry.updatePopularityAdjustingFactor(popularityAdjustingFactor);

    // for each entry above minimum popularity, adjust popularity counter, calculate bonus and
    // add to list
    System.out.println("entriesMap size: " + entriesMap.size());
    ArrayList<Thread> checkers = new ArrayList<Thread>();

    for (String key : entriesMap.keySet()) {
      Entry entry = entriesMap.get(key);
      entry.calculatePopularity(counterForInputSeries);
      if (entry.getPopularity() >= minPopularity) {
        Thread checker = new Thread(new MALEntryChecker(entries, entry, staff, genres));
        checkers.add(checker);
        checker.start();
      }
    }
    for (Thread checker : checkers) {
      try {
        checker.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("total recommendations: " + entries.size());

    // sort list of entries by popularity
    Collections.sort(entries);
    Collections.reverse(entries);
    return entries;
  }

  public static String absoluteUrl(String url) throws PageUnavailableException {
    PageScrapper scrapper = PageScrapper.fromUrl(url);
    return scrapper.selectFirstElement(detailsSelector).attr("href");
  }

}
