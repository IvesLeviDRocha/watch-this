package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.Source;
import com.weebly.niseishun.watchthis.view.MainGUI;

public class MainController {

  private MainGUI gui;
  private QueryHandler queryHandler;
  private InputParser inputParser;

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
    gui = new MainGUI(this);
    queryHandler = new QueryHandler();
    inputParser = new InputParser();
  }

  public void urlSearch(String url) {
    Source source = inputParser.parseInput(url);
    gui.showLoading();
    ArrayList<Entry> results = queryHandler.getRecommendationsWithURL(url, source);
    gui.displayResults(results);
  }

}
