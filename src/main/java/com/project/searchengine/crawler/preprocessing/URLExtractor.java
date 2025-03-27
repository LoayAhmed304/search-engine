package com.project.searchengine.crawler.preprocessing;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class URLExtractor {
    /**
     * Fetches the document from the passed URL.
     * 
     * @param url the URL from which the document is to be fetched
     * @return the document fetched from the URL
     */
    public static Document getDocument(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc;
        } catch (Exception e) {
            System.out.println("Error fetching the document from the URL: " + url);
            return null;
        }
    }

    /**
     * Extracts the URLs from a passed document.
     * 
     * @param doc the document from which the URLs are to be extracted
     * @see filterURLs
     * @return the set of filtered URLs extracted from the document
     */
    public static Set<String> getURLs(Document doc) {
        Set<String> urls = new HashSet<>();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String absUrl = link.attr("abs:href");
            urls.add(absUrl);
        }
        return filterURLs(urls);
    }

    /**
     * Works as a filter to exclude fragments, JavaScript, or mailto links.
     * 
     * @param urls set of URLs extracted from the document
     * @return the filtered set of URLs
     */
    public static Set<String> filterURLs(Set<String> urls) {
        Set<String> filteredURLs = new HashSet<>();
        for (String url : urls)
            if (url.startsWith("javascript") || url.startsWith("mailto"))
                continue;
            else
                filteredURLs.add(url);
        return filteredURLs;
    }

    public static void main(String[] args) {
        Document doc = getDocument("https://habibayman.github.io/web-crawler/");
        Set<String> urls = getURLs(doc);
        System.out.println("Document:" + doc);
        System.out.println("URLs extracted from the document:");
        for (String url : urls)
            System.out.println(url);
    }
}
