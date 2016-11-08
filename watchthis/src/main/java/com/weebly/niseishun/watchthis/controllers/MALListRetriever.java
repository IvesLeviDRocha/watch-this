package com.weebly.niseishun.watchthis.controllers;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.weebly.niseishun.watchthis.exception.PageUnavailableException;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

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
        String mean = userList.selectFirstElement(MALSearcher.categoryTotalsSelector).html();
        int index = mean.indexOf("Mean Score: ");
        index = index + 12;
        float userMeanScore = Float.valueOf(mean.substring(index, index + 3));
        ArrayList<Entry> userLikedSeries = new ArrayList<Entry>();

        String apiUrl =
            MALSearcher.malAPIurlPrefix + user.getUsername() + MALSearcher.malAPIurlSufix;

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
            String entryUrl = MALSearcher.seriesPagePrefix + seriesId;
            Entry entry = new Entry(seriesTitle, entryUrl);
            userLikedSeries.add(entry);
          } else {
            if (inputSeries) {
              relevant = false;
              break;
            }
          }

        }

        // if user is relevant, add data to map
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
      } catch (PageUnavailableException pue) {
        System.out.println("cannot access page");
        stop = true;
        break;
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
