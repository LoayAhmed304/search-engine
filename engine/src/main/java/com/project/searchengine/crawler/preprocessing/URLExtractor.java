package com.project.searchengine.crawler.preprocessing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class URLExtractor {
    /**
     * A reusable connection to be used for fetching documents.
     * This is a static field to avoid creating a new connection for each request.
     */
    private static final Connection connection = Jsoup.newSession()
            .timeout(5_000) 
            .ignoreHttpErrors(true) // Don't throw exceptions on HTTP errors
            .followRedirects(true) 
            .maxBodySize(2_000_000); 

    /**
     * Fetches the document from the passed URL using a reusable connection.
     */
    public static Document getDocument(String url) {
        try {
            Connection.Response res = connection.url(url).execute();

            // Only accept successful (200) responses with HTML content
            if (res.statusCode() == 200 &&
                    res.contentType() != null &&
                    res.contentType().contains("text/html")) {
                return res.parse();
            }
            System.out.println("Skipping non-HTML or failed response: " + url);
            return null;
        } catch (Exception e) {
            System.out.println("Error fetching URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts and filters URLs from a document.
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
     * Filters out invalid URLs (non-HTTP, malformed, etc.).
     */
    public static Set<String> filterURLs(Set<String> urls) {
        Set<String> filteredUrls = new HashSet<>();
        for (String url : urls) {
            try {
                String cleanedUrl = url.replace(" ", "%20");
                URI uri = new URI(cleanedUrl);
                String scheme = uri.getScheme();

                if (isUnwantedScheme(scheme))
                    continue;
                if (isValidUri(uri))
                    filteredUrls.add(url);

            } catch (URISyntaxException e) {
                continue; // Skip malformed URLs
            }
        }
        return filteredUrls;
    }

    /**
     * Checks if the URL scheme is unwanted (e.g., javascript, mailto).
     */
    private static boolean isUnwantedScheme(String scheme) {
        if (scheme == null)
            return false;
        return scheme.equalsIgnoreCase("javascript") ||
                scheme.equalsIgnoreCase("mailto") ||
                scheme.equalsIgnoreCase("tel") ||
                scheme.equalsIgnoreCase("sms") ||
                !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
    }

    /**
     * Checks if the URI is valid (has a host or a non-empty path).
     */
    private static boolean isValidUri(URI uri) {
        return uri.getHost() != null ||
                (uri.getPath() != null && !uri.getPath().isEmpty());
    }
}