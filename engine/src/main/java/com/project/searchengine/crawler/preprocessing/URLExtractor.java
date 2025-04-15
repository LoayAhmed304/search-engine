package com.project.searchengine.crawler.preprocessing;

import java.net.URI;
import java.net.URISyntaxException;
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
        // output the length of the links
        System.out.println("All URLs: " + links.size());
        for (Element link : links) {
            String absUrl = link.attr("abs:href");
            System.out.println(absUrl);
            urls.add(absUrl);
        }
        return filterURLs(urls);
    }

    /**
     * Filters URLs to exclude invalid schemes (javascript, mailto) and malformed
     * URLs.
     * Uses URI parsing for O(1) validation, better than regex.
     * 
     * @param urls set of URLs to filter
     * @return filtered set of valid URLs
     */
    public static Set<String> filterURLs(Set<String> urls) {
        Set<String> filteredUrls = new HashSet<>();

        for (String url : urls) {
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme();

                if (isUnwantedScheme(scheme))
                    continue;

                if (isValidUri(uri))
                    filteredUrls.add(url);

            } catch (URISyntaxException e) {
                System.err.println("Invalid URL syntax: " + url);
                continue;
            }
        }

        return filteredUrls;
    }

    private static boolean isUnwantedScheme(String scheme) {
        if (scheme == null)
            return false; // Allow protocol-relative URLs -> handled in URLNormalizer

        // List of unwanted schemes
        return scheme.equalsIgnoreCase("javascript") ||
                scheme.equalsIgnoreCase("mailto") ||
                scheme.equalsIgnoreCase("tel") ||
                scheme.equalsIgnoreCase("sms");
    }

    private static boolean isValidUri(URI uri) {
        return (uri.getHost() != null) ||
                (uri.getPath() != null && !uri.getPath().isEmpty());
    }

}