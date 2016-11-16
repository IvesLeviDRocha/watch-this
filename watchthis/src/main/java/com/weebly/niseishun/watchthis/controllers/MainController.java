package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;

import com.weebly.niseishun.watchthis.exception.InvalidURLException;
import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.Source;
import com.weebly.niseishun.watchthis.view.MainGUI;

public class MainController {

  private MainGUI gui;
  private QueryHandler queryHandler;
  private InputParser inputParser;

  public static final String INVALID_URL_MESSAGE =
      "<html>Could not search input URL. <br/> Please make sure the input is a valid source URL.</html>";

  public static final String UNKNOWN_ERROR = "An unknown error occurred.";

  /**
   * run program loop
   */
  public void start() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        init();
        gui.launch();
      }
    });
  }

  /**
   * initial setup
   */
  private void init() {
    RequestManager.init();
    gui = new MainGUI(this);
    queryHandler = new QueryHandler();
    inputParser = new InputParser();
  }

  public void urlSearch(String url) {
    try {
      // get source
      Source source = inputParser.parseInput(url);
      // show loading screen while searching for results
      gui.showLoading();
      // search for results
      ArrayList<Entry> results = queryHandler.getRecommendationsWithURL(url, source);
      // display results
      gui.displayResults(results);
    } catch (InvalidURLException e) {
      gui.showMessageAndStandby(INVALID_URL_MESSAGE);
    } catch (Exception e) {
      gui.showMessageAndStandby(UNKNOWN_ERROR);
    }

  }

}
