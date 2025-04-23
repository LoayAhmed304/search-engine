package com.project.searchengine.crawler;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is responsible for handling robots.txt files with shared caching.
 * It checks if a given URL is allowed to be crawled based on the rules defined
 * in the robots.txt file of the domain.
 * Assumes that the passed url is already normalized.
 */
public class RobotsHandler {
    private static final String USER_AGENT = "*";
    private final SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

    // Static shared cache across all instances
    private static final ConcurrentMap<String, BaseRobotRules> SHARED_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 4000;

    /**
     * @param url the URL to check
     * @return true if the URL is allowed to be crawled, false otherwise
     */
    public boolean isUrlAllowed(String url) {
        try {
            URI uri = new URI(url);
            String domainKey = getDomainKey(uri);

            BaseRobotRules rules = getRules(domainKey);

            return rules.isAllowed(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    /**
     * @param robotsTxtUrl the constructed URL for the robots.txt file
     * @return the HTTP response from the robots.txt URL reading request
     * @throws IOException to be handled by @see isUrlAllowed
     *                     It fetches non-html(plain text) content with maximum
     *                     timeout of 10 seconds.
     */
    private Connection.Response fetchRobotsTxt(String robotsTxtUrl) throws IOException {
        return Jsoup.connect(robotsTxtUrl)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .timeout(10_000)
                .execute();
    }

    /**
     * Generates the robots.txt URL from a given URI
     */
    private String getRobotsTxtUrl(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority() + "/robots.txt";
    }

    /**
     * Generates a consistent domain key for caching
     */
    private String getDomainKey(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    /**
     * @param domainKey the domain key for the robots.txt file
     * @return the BaseRobotRules object for the domain
     * Checks if the rules are already cached and if not, fetches, caches and parses.
     */
    BaseRobotRules getRules(String domainKey) {
        BaseRobotRules rules = SHARED_CACHE.get(domainKey);
        if(rules == null) {
            try {
                URI uri = new URI(domainKey);
                String robotsTxtUrl = getRobotsTxtUrl(uri);
                Connection.Response response = fetchRobotsTxt(robotsTxtUrl);
                rules = parser.parseContent(
                        robotsTxtUrl,
                        response.bodyAsBytes(),
                        response.contentType(),
                        USER_AGENT);
                
                addToCache(domainKey, rules);
            } catch (IOException | URISyntaxException e) {
                // Return default permissive rules if fetch fails
                rules = parser.parseContent("", new byte[0], "text/plain", USER_AGENT);
            }
        }
        return rules;
    }

    /**
     * @param key the domain key for the robots.txt file
     * @param rules the BaseRobotRules object to cache
     * Works as a cache manager for the class
     */
    public static void addToCache(String key, BaseRobotRules rules) {
        if (SHARED_CACHE.size() < MAX_CACHE_SIZE) 
            SHARED_CACHE.putIfAbsent(key, rules);
        else
        {
            clearSharedCache();
            SHARED_CACHE.putIfAbsent(key, rules); 
        }
    }

    /**
     * Clears the shared cache (available to all instances)
     */
    public static void clearSharedCache() {
        SHARED_CACHE.clear();
    }
}