package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

public class MALSearcher {

  private String userSelector = "table.table-recently-updated > tbody > tr:not(:first-child)";
  private String nameSelector = "td.borderClass.di-t.w100:first-child > div.di-tc.va-m.al.pl4 > a";
  private String listUrlPrefix = "https://myanimelist.net/animelist/";
  private String listUrlSuffix = "?status=2";
  private String statusSelector = "td:nth-child(3)";
  private String statsSuffix = "/stats?m=all&show=";
  private int pageElementsIncrement = 75;
  private String categoryTotalsSelector = ":contains(Mean Score:)";
  private String listLineSelector = "tbody.list-item > tr.list-table-data";
  private String lineScoreSelector = "td.data.score";
  private String lineTitleSelector = "td.data.title.clearfix > a.link.sort";
  private String seriesPagePrefix = "https://myanimelist.net";
  private String malAPIurlPrefix = "http://myanimelist.net/malappinfo.php?u=";
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
      System.out.println("user: " + user.getUsername());
      try {
        PageScrapper userList = PageScrapper.fromUrl(user.getListUrl());
        String test = userList.selectFirstElement(categoryTotalsSelector).html();
        int index = test.indexOf("Mean Score: ");
        index = index + 12;
        float userMeanScore = Float.valueOf(test.substring(index, index + 3));
        System.out.println(userMeanScore);


        String apiUrl = malAPIurlPrefix + user.getUsername() + malAPIurlSufix;
        System.out.println(apiUrl);

        URL url = new URL(apiUrl);
        URLConnection connection = url.openConnection();

        Document doc = parseXML(connection.getInputStream());

        NodeList descNodes = doc.getElementsByTagName("anime");

        // for each anime node
        for (int i = 0; i < descNodes.getLength(); i++) {
          // get children
          Node node = descNodes.item(i);
          NodeList children = node.getChildNodes();
          // for each child
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
          System.out.println(completed);
        }


        /*
         * Elements listLines = userList.selectElements(listLineSelector); ArrayList<String>
         * likedSeries = new ArrayList<String>(); boolean likedInputSeries = false; for (Element
         * element : listLines) { System.out.println("Element: " + element.toString()); Element
         * titleElement = element.select(lineTitleSelector).first(); String title =
         * titleElement.html(); System.out.println("Checking: " + title); float score =
         * Float.valueOf(element.select(lineScoreSelector).first().html()); if
         * (seriesName.equals(title)) { if (score < userMeanScore) { break; } else {
         * likedInputSeries = true; continue; } } if (score >= userMeanScore) { String
         * likedSeriesUrl = seriesPagePrefix + titleElement.attr("href"); likedSeries.add(title); }
         * } if (likedInputSeries) { for (String series : likedSeries) { String[] seriesUrlParts =
         * series.split("/"); String seriesTitle = seriesUrlParts[seriesUrlParts.length -
         * 1].replaceAll("_", " "); if (entriesMap.containsKey(seriesTitle)) {
         * entriesMap.get(seriesTitle).incrementCounter(); } else { Entry entry = new
         * Entry(seriesTitle, series); entriesMap.put(seriesTitle, entry); } } }
         */
      } catch (Exception e) {
        System.out.println("Could not access list");
        e.printStackTrace();
      }
    }
    Collection<Entry> entryCollection = entriesMap.values();
    ArrayList<Entry> entries = new ArrayList<Entry>(entryCollection);
    for (Entry entry : entries) {
      if (entry.getCounter() < minLikes) {
        System.out.println("REMOVING: " + entry.getTitle() + " || Counter: " + entry.getCounter());
        entries.remove(entry);
      } else {
        System.out.println("KEEPING: " + entry.getTitle() + " || Counter: " + entry.getCounter());
      }
    }
    Collections.sort(entries);
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
      throw ex;
    }

    return doc;
  }

}
