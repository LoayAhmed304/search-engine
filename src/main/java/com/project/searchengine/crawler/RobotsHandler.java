package com.project.searchengine.crawler;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import org.jsoup.*;
import java.io.IOException;
import java.net.*;

/**
 * This class is responsible for handling robots.txt files.
 * It checks if a given URL is allowed to be crawled based on the rules defined
 * in the robots.txt file of the domain.
 * Assumes that the passed url is already normalized.
 */

public class RobotsHandler {
    private static final String USER_AGENT = "*";
    private final SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

    /**
     * @param url the URL to check
     * @return true if the URL is allowed to be crawled, false otherwise
     */
    public boolean isUrlAllowed(String url) {
        try {
            URI uri = new URI(url);
            String robotsTxtUrl = getRobotsTxtUrl(uri);

            Connection.Response response = fetchRobotsTxt(robotsTxtUrl);

            BaseRobotRules rules = parser.parseContent(
                    robotsTxtUrl,
                    response.bodyAsBytes(),
                    response.contentType(),
                    USER_AGENT);

            return rules.isAllowed(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        } catch (IOException e) {
            return true; // Allow crawling if robots.txt doesn't exist or is inaccessible
        }
    }

    /**
     * @param robotsTxtUrl the constructed URL for the robots.txt file
     * @return the HTTP response from the robots.txt URL reading request
     * @throws IOException to be handled by @see isUrlAllowed
     * It fatches non-html(plain text) content with maximum timeout of 10 seconds.
     */
    private Connection.Response fetchRobotsTxt(String robotsTxtUrl) throws IOException {
        Connection.Response response = Jsoup.connect(robotsTxtUrl)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .timeout(10_000)
                .execute();
        return response;
    }

    private String getRobotsTxtUrl(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority() + "/robots.txt";
    }
}
