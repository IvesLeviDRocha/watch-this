package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.nodes.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.weebly.niseishun.watchthis.exception.PageUnavailableException;
import com.weebly.niseishun.watchthis.exception.ParsingException;
import com.weebly.niseishun.watchthis.exception.SeriesNotRelevantException;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

class MALListRetriever implements Runnable {

  private User user;
  private ConcurrentHashMap<String, Entry> entriesMap;
  private String inputSeriesTitle;

  private float userMeanScore;

  public MALListRetriever(User user, ConcurrentHashMap<String, Entry> entriesMap,
      String inputSeriesTitle) {
    this.user = user;
    this.entriesMap = entriesMap;
    this.inputSeriesTitle = inputSeriesTitle;
  }

  public void run() {
    try {
      retrieveUserMeanScore();
      retrieveUserLikedSeries();
    } catch (ParsingException e) {
      // error
    }
  }

  private void retrieveUserMeanScore() throws ParsingException {
    try {
      // get user's list html content
      PageScrapper userList = PageScrapper.fromUrl(user.getListUrl());
      // get mean score from user's list
      Element meanElement = userList.selectFirstElement(MALSearcher.categoryTotalsSelector);
      if (meanElement == null) {
        System.out.println("null element : could not parse user's list");
        System.out.println("user: " + user.getUsername() + " | url: " + user.getListUrl());
        throw new ParsingException();
      }
      String mean = meanElement.html();
      int index = mean.indexOf("Mean Score: ");
      index = index + 12;
      try {
        userMeanScore = Float.valueOf(mean.substring(index, index + 3));
      } catch (NumberFormatException e) {
        System.out.println("number format exception");
        throw new ParsingException();
      }
    } catch (PageUnavailableException e) {
      throw new ParsingException();
    }
  }


  private void retrieveUserLikedSeries() throws ParsingException {
    ArrayList<Entry> userLikedSeries = new ArrayList<Entry>();
    try {
      String apiUrl = MALSearcher.malAPIurlPrefix + user.getUsername() + MALSearcher.malAPIurlSufix;
      Document doc = getDoc(apiUrl);
      NodeList seriesNodes = doc.getElementsByTagName("anime");
      addAllSeriesToList(userLikedSeries, seriesNodes);
      addListDataToMap(userLikedSeries);
    } catch (Exception e) {
      throw new ParsingException();
    }
  }

  private void addAllSeriesToList(ArrayList<Entry> userLikedSeries, NodeList seriesNodes)
      throws SeriesNotRelevantException {
    // for each series node
    for (int i = 0; i < seriesNodes.getLength(); i++) {
      // get data
      Node series = seriesNodes.item(i);
      NodeList seriesData = series.getChildNodes();
      // add to list
      addSeriesToList(userLikedSeries, seriesData);
    }
  }

  private void addListDataToMap(ArrayList<Entry> userLikedSeries) {
    for (Entry entry : userLikedSeries) {
      String seriesTitle = entry.getTitle();
      if (entriesMap.containsKey(entry.getTitle())) {
        entriesMap.get(seriesTitle).incrementCounter();
      } else {
        entriesMap.put(seriesTitle, entry);
      }
    }
  }

  private void addSeriesToList(ArrayList<Entry> userLikedSeries, NodeList seriesData)
      throws SeriesNotRelevantException {
    String seriesTitle = getSeriesTitle(seriesData);
    // check if input series
    boolean inputSeries = false;
    if (seriesTitle.equals(inputSeriesTitle)) {
      inputSeries = true;
    }

    if (!inputSeries) {
      // check if completed (only matters if its not the input)
      boolean completed = checkSeriesCompleted(seriesData);
      if (!completed && !inputSeries) {
        return;
      }
    }

    // get score and check if its above mean score
    float seriesScore = getSeriesScore(seriesData);

    // check if above mean score
    if (seriesScore >= userMeanScore) {
      // get series url
      String entryUrl = getSeriesUrl(seriesData);
      Entry entry = new Entry(seriesTitle, entryUrl);
      userLikedSeries.add(entry);
    } else {
      if (inputSeries) {
        throw new SeriesNotRelevantException();
      }
    }
  }

  private String getSeriesUrl(NodeList seriesData) {
    String seriesId = "";
    for (int j = 0; j < seriesData.getLength(); j++) {
      Node childNode = seriesData.item(j);
      if (childNode.getNodeName().equals("series_animedb_id")) {
        seriesId = childNode.getTextContent();
        break;
      }
    }
    String entryUrl = MALSearcher.seriesPagePrefix + seriesId;
    return entryUrl;
  }

  private float getSeriesScore(NodeList seriesData) {
    float seriesScore = 0f;
    for (int j = 0; j < seriesData.getLength(); j++) {
      Node childNode = seriesData.item(j);
      if (childNode.getNodeName().equals("my_score")) {
        seriesScore = Float.valueOf(childNode.getTextContent());
        break;
      }
    }
    return seriesScore;
  }

  private boolean checkSeriesCompleted(NodeList seriesData) {
    boolean completed = false;
    for (int j = 0; j < seriesData.getLength(); j++) {
      Node childNode = seriesData.item(j);
      if (childNode.getNodeName().equals("my_status")) {
        int status = Integer.valueOf(childNode.getTextContent());
        if (status == 2) {
          completed = true;
        }
        break;
      }
    }
    return completed;
  }

  private String getSeriesTitle(NodeList children) {
    String seriesTitle = "";
    for (int j = 0; j < children.getLength(); j++) {
      Node childNode = children.item(j);
      if (childNode.getNodeName().equals("series_title")) {
        seriesTitle = childNode.getTextContent();
        break;
      }
    }
    return seriesTitle;
  }

  private Document getDoc(String apiUrl) {
    URL url;
    Document doc;
    try {
      url = new URL(apiUrl);
    } catch (MalformedURLException e) {
      System.out.println("malformed url");
      return null;
    }
    while (true) {
      boolean permission = false;
      while (!permission) {
        permission = RequestManager.requestPermission(RequestManager.MALAPI);
      }
      URLConnection connection;
      try {
        connection = url.openConnection();
        doc = parseXML(connection.getInputStream());
        return doc;
      } catch (IOException e) {
        if (e.getMessage().contains("429")) {
          System.out.println("429 in getConnection");
          continue;
        }
        break;
      } catch (Exception e) {
        System.out.println("parse problem?");
        e.printStackTrace();
        return null;
      }
    }
    return null;
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
