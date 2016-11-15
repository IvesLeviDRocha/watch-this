package com.weebly.niseishun.watchthis.controllers;

import com.weebly.niseishun.watchthis.model.Source;

public class InputParser {

  /**
   * check user input url for source
   * 
   * @param user input url
   * @return source type of url
   */
  public Source parseInput(String input) {
    return Source.MAL;
  }

}
