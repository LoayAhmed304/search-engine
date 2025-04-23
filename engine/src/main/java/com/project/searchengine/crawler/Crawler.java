package com.project.searchengine.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import org.jsoup.nodes.*;

import com.project.searchengine.crawler.preprocessing.*;
import com.project.searchengine.utils.HashManager;

@Component
public class Crawler {

    private final UrlsFrontier urlsFrontier;
    private RobotsHandler robotsHandler;
    private static int currentBatch = 1;

    @Autowired
    public Crawler(UrlsFrontier urlsFrontier) {
        this.urlsFrontier = urlsFrontier;
        this.robotsHandler = new RobotsHandler();
    }

    /**
     * Handles the initialization of the crawling process: seeding, preparing caches, etc.
     */
    public void initCrawling() {
        if (urlsFrontier.shouldInitializeFrontier())
            urlsFrontier.seedFrontier();
        urlsFrontier.getAllHashedDocContent(); // Prepare the cache
    }

    /**
     * Crawls the URLs managed by the frontier.
     * It processes each URL in the current batch, fetches its content,
     * checks for duplicates, and handles linked pages.
     */
    public void crawl() {
        System.out.println("Starting the crawling process...");
        initCrawling();

        while (urlsFrontier.getNextUrlsBatch()) {
            System.out.println("\nProcessing batch of URLs number: " + currentBatch++);

            urlsFrontier.currentUrlBatch.stream()
                    .forEach(url -> {
                        System.out.println("Crawling URL: " + url);

                        Document pageContent = URLExtractor.getDocument(url);

                        if (pageContent == null) {
                            System.out.println("Failed to fetch content for URL: " + url);
                            urlsFrontier.removeUrl(url);
                            return;
                        }

                        String hashedDocument = HashManager.hash(pageContent.toString());

                        if (urlsFrontier.isDuplicate(hashedDocument)) {
                            System.out.println("Duplicate document found. Skipping URL: " + url);
                            urlsFrontier.removeUrl(url);
                            return;
                        }

                        Set<String> allLinks = URLExtractor.getURLs(pageContent);
                        List<String> linkedPages = new ArrayList<>(allLinks);

                        handleLinkedPages(linkedPages);

                        urlsFrontier.saveCrawledDocument(url, pageContent.toString(), hashedDocument, linkedPages);
                    });
        }
        System.out.println("Finished processing total batch of URLs of count: " + (currentBatch - 1));
        System.out.println("Crawling process completed.");
    }

    /**
     * Seeds the frontier with URLs from the seed file.
     */
    public void seed() {
        System.out.println("Seeding the frontier...");
        urlsFrontier.seedFrontier();
        System.out.println("Frontier seeded successfully.");
    }

    /**
     * Handles the linked pages extracted from the crawled document.
     * It checks if each linked page is allowed to be crawled based on robots.txt
     * rules.
     *
     * @param linkedPages List of linked pages to handle
     */
    private void handleLinkedPages(List<String> linkedPages) {
        if (!urlsFrontier.hasReachedThreshold()) {
            for (String linkedUrl : linkedPages) {
                String normalizedUrl = URLNormalizer.normalizeUrl(linkedUrl);

                if (!robotsHandler.isUrlAllowed(normalizedUrl))
                    continue;

                if (!urlsFrontier.handleUrl(normalizedUrl))
                    break;
            }
        }
    }
}