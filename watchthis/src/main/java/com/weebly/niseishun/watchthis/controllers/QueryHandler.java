package com.weebly.niseishun.watchthis.controllers;

import java.util.ArrayList;

import com.weebly.niseishun.watchthis.model.Entry;
import com.weebly.niseishun.watchthis.model.Source;

public class QueryHandler {
	
	/**
	 * run algorithm to find recommendations for a given series
	 * @param url to series page
	 * @param source of series page
	 * @return list of recommendations
	 */
	public ArrayList<Entry> getRecommendationsWithURL(String url, Source source) {
		ArrayList<Entry> recommendations = new ArrayList<Entry>();
		//TODO
		return recommendations;
	}

}
