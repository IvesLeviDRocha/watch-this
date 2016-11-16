package com.weebly.niseishun.watchthis.controllers;

import com.weebly.niseishun.watchthis.exception.InvalidURLException;
import com.weebly.niseishun.watchthis.model.Source;

public class InputParser {

  /**
   * check user input url for source
   * 
   * @param user input url
   * @return source type of url
   * @throws InvalidURLException
   */
  public Source parseInput(String input) throws InvalidURLException {
    if (input == null) {
      throw new InvalidURLException();
    }
    if (input.length() < 30) {
      throw new InvalidURLException();
    }
    if (input.substring(0, 30).equalsIgnoreCase("https://myanimelist.net/anime/")) {
      return Source.MAL;
    }
    throw new InvalidURLException();
  }

}
