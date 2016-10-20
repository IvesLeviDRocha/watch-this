package com.weebly.niseishun.watchthis.controllers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Wrapper for Jsoup and document classes. Maintains only one document element for one page.
 * Use static factory method for instantiation.
 * @author Levi R
 *
 */
public class PageScrapper {
	
	private Document doc;
	
	/**
	 * Factory method, returns a page scrapper object from a page url.
	 * @param url of page
	 * @return page scrapper object
	 * @throws IOException
	 */
	public static PageScrapper fromUrl(String url) throws IOException {
		Document document = getDocFromUrl(url);
		return new PageScrapper(document);
	}
	
	private PageScrapper(Document doc) {
		this.doc = doc;
	}
	
	private static Document getDocFromUrl(String url) throws IOException {
		return Jsoup.connect(url).get();
	}
	
	/**
	 * get list of all elements matching selector
	 * @param selector 
	 * @return list of elements
	 */
	public Elements selectElements(String selector) {
		Elements elements = doc.select(selector);
		return elements;
	}
	
	/**
	 * get only first element matching selector
	 * @param selector
	 * @return first element to match selector
	 */
	public Element selectFirstElement(String selector) {
		Element element = doc.select(selector).first();
		return element;
	}
	
	

}
