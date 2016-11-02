package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
  private String statusSelector = "td:nth-child(3)";
  private String statsSuffix = "/stats?m=all&show=";
  private int pageElementsIncrement = 75;
  private String categoryTotalsSelector = ":contains(Mean Score:)";
  private String seriesPagePrefix = "https://myanimelist.net/anime/";
  private String malAPIurlPrefix = "https://myanimelist.net/malappinfo.php?u=";
  private String malAPIurlSufix = "&status=all&type=anime.";

  /**
   * get last users to update a series on MAL
   * 
   * @param url of series
   * @param numOfUsers to be returned
   * @return list of users with specified size
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
  public ArrayList<Entry> getRecommendedSeriesFromUsers(ArrayList<User> users, int minLikes,
      String seriesUrl) throws IOException {
    HashMap<String, Entry> entriesMap = new HashMap<String, Entry>();
    String[] urlElements = seriesUrl.split("/");
    String seriesName = urlElements[urlElements.length - 1].replaceAll("_", " ");
    for (User user : users) {
      try {
        PageScrapper userList = PageScrapper.fromUrl(user.getListUrl());
        String test = userList.selectFirstElement(categoryTotalsSelector).html();
        int index = test.indexOf("Mean Score: ");
        index = index + 12;
        float userMeanScore = Float.valueOf(test.substring(index, index + 3));


        String apiUrl = malAPIurlPrefix + user.getUsername() + malAPIurlSufix;

        URL url = new URL(apiUrl);
        URLConnection connection = url.openConnection();

        Document doc = parseXML(connection.getInputStream());

        NodeList descNodes = doc.getElementsByTagName("anime");

        // for each anime node
        for (int i = 0; i < descNodes.getLength(); i++) {
          // get children
          Node node = descNodes.item(i);
          NodeList children = node.getChildNodes();
          // check if completed
          boolean completed = false;
          for (int j = 0; i < children.getLength(); j++) {
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

          // get series name and check if its the input series
          String seriesTitle = "";
          for (int j = 0; i < children.getLength(); j++) {
            Node childNode = children.item(j);
            if (childNode.getNodeName().equals("series_title")) {
              seriesTitle = childNode.getTextContent();
              break;
            }
          }
          if (seriesTitle.equals(seriesName)) {
            continue;
          }

          // get score and check if its above mean score
          float seriesScore = 0f;
          for (int j = 0; i < children.getLength(); j++) {
            Node childNode = children.item(j);
            if (childNode.getNodeName().equals("my_score")) {
              seriesScore = Float.valueOf(childNode.getTextContent());
              break;
            }
          }
          if (seriesScore >= userMeanScore) {
            // get series url
            String seriesId = "";
            for (int j = 0; i < children.getLength(); j++) {
              Node childNode = children.item(j);
              if (childNode.getNodeName().equals("series_animedb_id")) {
                seriesId = childNode.getTextContent();
                break;
              }
            }
            // check if already in map
            if (entriesMap.containsKey(seriesTitle)) {
              entriesMap.get(seriesTitle).incrementCounter();
            } else {
              String seriesPageSufix = "/" + seriesTitle.replaceAll(" ", "_");
              String entryUrl = seriesPagePrefix + seriesId + seriesPageSufix;
              Entry entry = new Entry(seriesTitle, entryUrl);
              entriesMap.put(seriesTitle, entry);
            }

          }
        }
      } catch (Exception e) {
        //System.out.println("Could not access list");
      }
    }
    ArrayList<Entry> entries = new ArrayList<Entry>();
    for (String key : entriesMap.keySet()) {
      Entry entry = entriesMap.get(key);
      if (entry.getCounter() >= minLikes) {
        entries.add(entry);
      }
    }
    Collections.sort(entries);
    Collections.reverse(entries);
    return entries;
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
