package com.weebly.niseishun.watchthis.model;


/**
 * Entity representing a source's user, with an username and a url pointing to the user's list of
 * series.
 * 
 * @author Levi Rocha
 *
 */
public class User {

  private String username;
  private String listUrl;

  public User(String username, String listUrl) {
    super();
    this.username = username;
    this.listUrl = listUrl;
  }

  public String getListUrl() {
    return listUrl;
  }

  public String getUsername() {
    return username;
  }

}
