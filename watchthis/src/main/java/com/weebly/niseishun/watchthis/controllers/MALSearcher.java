package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.User;

public class MALSearcher {
	
	/**
	 * get last users to update a series on MAL
	 * @param url of series
	 * @param numOfUsers to be returned
	 * @return list of users 
	 */
	public ArrayList<User> getLastUpdatedUsers(String url, int numOfUsers) {
		ArrayList<User> users = new ArrayList<User>();
		//TODO
		return users;
	}
	
	/**
	 * get the most liked series by given users
	 * @param users whose lists should be checked
	 * @param minLikes minimum number of users to have liked a series for it to be recommended
	 * @return list of recommended series
	 */
	public ArrayList<Entry> getRecommendedSeriesFromUsers(ArrayList<User> users, int minLikes) {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		//TODO
		return entries;
	}

}
