package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

/**
 * This searcher implements the functionalities pertaining to the source MyAnimeList.
 * 
 * @author Levi Rocha
 *
 */
public class MALSearcher {

  private String userSelector = "table.table-recently-updated > tbody > tr:not(:first-child)";
  private String nameSelector = "td.borderClass.di-t.w100:first-child > div.di-tc.va-m.al.pl4 > a";
  private String listUrlPrefix = "https://myanimelist.net/animelist/";
  private String listUrlSuffix = "?status=2";
  private String statsSuffix = "/stats?m=all&show=";
  private int pageElementsIncrement = 75;
  private String categoryTotalsSelector = ":contains(Mean Score:)";
  private String seriesPagePrefix = "https://myanimelist.net/anime/";
  private String malAPIurlPrefix = "https://myanimelist.net/malappinfo.php?u=";
  private String malAPIurlSufix = "&status=all&type=anime.";
  private String titleSelector = "#contentWrapper > div:nth-child(1) > h1 > span";
  private String scoreSelectorFromStats = "td:nth-child(2)";
  private String detailsSelector = "#horiznav_nav > ul > li:nth-child(1) > a";

  private String url;

  public MALSearcher(String url) {
    PageScrapper inputPage;
    try {
      inputPage = PageScrapper.fromUrl(url);
      this.url = inputPage.selectFirstElement(detailsSelector).attr("href");
    } catch (IOException e) {
      System.out.println("error connecting");
      e.printStackTrace();
      this.url = url;
    }
    System.out.println("mal searcher created for " + this.url);
  }

  /**
   * get last users to update a series on MAL
   * 
   * @param url of series
   * @param numOfUsers to be returned
   * @return list of users with specified size
   * @throws IOException
   */
  public ArrayList<User> getLastUpdatedUsers(int numOfUsers) throws IOException {
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
   * @throws IOException
   */
  public ArrayList<Entry> getRecommendedSeriesFromUsers(ArrayList<User> users, int minLikes)
      throws IOException {
    int simultaneousThreads = 6;
    ConcurrentHashMap<String, Entry> entriesMap = new ConcurrentHashMap<String, Entry>();

    PageScrapper seriesPage = PageScrapper.fromUrl(url);
    String seriesName = seriesPage.selectFirstElement(titleSelector).html();

    for (int i = 0; i < users.size(); i += simultaneousThreads) {
      ArrayList<Thread> retrievers = new ArrayList<Thread>();
      for (int j = i; j < i + simultaneousThreads; j++) {
        if (j < users.size()) {
          User user = users.get(j);
          Thread retriever = new Thread(new MALListRetriever(user, entriesMap, seriesName));
          retrievers.add(retriever);
          retriever.start();
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      for (Thread retriever : retrievers) {
        try {
          retriever.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    ArrayList<Entry> entries = new ArrayList<Entry>();

    int counterForInputSeries = entriesMap.get(seriesName).getCounter();
    System.out.println("relevant users checked: " + counterForInputSeries);
    entriesMap.remove(seriesName);

    for (String key : entriesMap.keySet()) {
      Entry entry = entriesMap.get(key);
      if (entry.getCounter() >= minLikes) {
        entry.updateCounterRelativeToMaxPopularity(counterForInputSeries);
        entries.add(entry);
      }
    }
    Collections.sort(entries);
    Collections.reverse(entries);
    return entries;
  }

  class MALListRetriever implements Runnable {

    private User user;
    private ConcurrentHashMap<String, Entry> entriesMap;
    private String inputSeriesTitle;

    public MALListRetriever(User user, ConcurrentHashMap<String, Entry> entriesMap,
        String inputSeriesTitle) {
      this.user = user;
      this.entriesMap = entriesMap;
      this.inputSeriesTitle = inputSeriesTitle;
    }

    public void run() {
      boolean stop = false;
      int tries = 0;
      while (!stop) {
        try {
          PageScrapper userList = PageScrapper.fromUrl(user.getListUrl());
          String mean = userList.selectFirstElement(categoryTotalsSelector).html();
          int index = mean.indexOf("Mean Score: ");
          index = index + 12;
          float userMeanScore = Float.valueOf(mean.substring(index, index + 3));
          ArrayList<Entry> userLikedSeries = new ArrayList<Entry>();

          String apiUrl = malAPIurlPrefix + user.getUsername() + malAPIurlSufix;


          URL url = new URL(apiUrl);
          URLConnection connection = url.openConnection();

          Document doc = parseXML(connection.getInputStream());

          NodeList descNodes = doc.getElementsByTagName("anime");

          boolean relevant = false;

          // for each anime node
          for (int i = 0; i < descNodes.getLength(); i++) {
            // get children
            Node node = descNodes.item(i);
            NodeList children = node.getChildNodes();

            // get series name
            String seriesTitle = "";
            for (int j = 0; j < children.getLength(); j++) {
              Node childNode = children.item(j);
              if (childNode.getNodeName().equals("series_title")) {
                seriesTitle = childNode.getTextContent();
                break;
              }
            }

            // check if input series
            boolean inputSeries = false;
            if (seriesTitle.equals(inputSeriesTitle)) {
              inputSeries = true;
            }

            if (!inputSeries) {
              // check if completed (only matters if its not the input)
              boolean completed = false;
              for (int j = 0; j < children.getLength(); j++) {
                Node childNode = children.item(j);
                if (childNode.getNodeName().equals("my_status")) {
                  int status = Integer.valueOf(childNode.getTextContent());
                  if (status == 2) {
                    completed = true;
                  }
                  break;
                }
              }
              if (!completed) {
                continue;
              }
            }

            // get score and check if its above mean score
            float seriesScore = 0f;
            for (int j = 0; j < children.getLength(); j++) {
              Node childNode = children.item(j);
              if (childNode.getNodeName().equals("my_score")) {
                seriesScore = Float.valueOf(childNode.getTextContent());
                break;
              }
            }


            // check if above mean score
            if (seriesScore >= userMeanScore) {
              if (inputSeries) {
                relevant = true;
              }

              // get series url
              String seriesId = "";
              for (int j = 0; j < children.getLength(); j++) {
                Node childNode = children.item(j);
                if (childNode.getNodeName().equals("series_animedb_id")) {
                  seriesId = childNode.getTextContent();
                  break;
                }
              }
              String entryUrl = seriesPagePrefix + seriesId;
              Entry entry = new Entry(seriesTitle, entryUrl);
              userLikedSeries.add(entry);
            } else {
              if (inputSeries) {
                relevant = false;
                break;
              }
            }

          }
          if (relevant) {
            for (Entry entry : userLikedSeries) {
              String seriesTitle = entry.getTitle();
              if (entriesMap.containsKey(entry.getTitle())) {
                entriesMap.get(seriesTitle).incrementCounter();
              } else {
                entriesMap.put(seriesTitle, entry);
              }
            }
          }
          stop = true;
        } catch (Exception e) {
          tries++;
          if (tries > 1) {
            stop = true;
            break;
          }
          try {
            Thread.sleep(200);
          } catch (InterruptedException e1) {
            System.out.println("could not wait");
          }
        }
      }
    }

  }

  private Document parseXML(InputStream stream) throws Exception {
    DocumentBuilderFactory objDocumentBuilderFactory = null;
    DocumentBuilder objDocumentBuilder = null;
    Document doc = null;
    try {
      objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
      objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
      doc = objDocumentBuilder.parse(stream);
    } catch (Exception ex) {
      System.out.println("throwing ex when parsing xml");
      throw ex;
    }

    return doc;
  }

}
