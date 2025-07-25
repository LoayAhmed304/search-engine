package com.project.searchengine.crawler;

import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.service.UrlsFrontierService;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlsFrontier {

    private UrlsFrontierService urlsFrontierService;

    private final String SEEDS_FILE_PATH = Paths.get("src/main/resources/seeds-cartoons.txt").toString();
    public static final int BATCH_SIZE = 100;
    public static final int MAX_URLS = 6000;
    public List<String> currentUrlBatch = Collections.synchronizedList(new ArrayList<>());
    public Set<String> hashedDocsCache = ConcurrentHashMap.newKeySet();

    /**
     * Constructor for UrlsFrontier.
     *
     * @param urlsFrontierService Service to manage URLs in the frontier.
     */
    @Autowired
    public UrlsFrontier(UrlsFrontierService urlsFrontierService) {
        this.urlsFrontierService = urlsFrontierService;
    }

    /**
     * Initializes the frontier with a list of seed URLs.
     *
     * @param seedUrls List of seed URLs to initialize the frontier.
     */
    public void seedFrontier() {
        List<String> seedUrls = readSeeds();
        System.out.println("Seeding the frontier with " + seedUrls.size() + " URLs." + seedUrls);
        urlsFrontierService.initializeFrontier(seedUrls, MAX_URLS);
    }

    /**
     * Utility method
     * Reads seed URLs from a file.
     *
     * @return List of seed URLs
     */
    private List<String> readSeeds() {
        List<String> seeds = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(SEEDS_FILE_PATH))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                seeds.add(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + SEEDS_FILE_PATH);
        }
        return seeds;
    }

    /**
     * Retrieves the next batch of URLs from the frontier.
     *
     * @return true if the batch was successfully retrieved, false if no more URLs are available.
     */
    public synchronized boolean getNextUrlsBatch() {
        List<String> urls = urlsFrontierService.getTop100UrlsByFrequency();
        if (urls.isEmpty()) {
            return false;
        }
        currentUrlBatch.clear();
        currentUrlBatch.addAll(urls);
        return true;
    }

    /**
     * Retrieves all hashed document contents from the database.
     * Sets the allHashedDocs Cache.
     * This is used to check for duplicates in the frontier.
     */
    public void getAllHashedDocContent() {
        hashedDocsCache.addAll(urlsFrontierService.findAllHashedDocContent());
    }

    /**
     * Handles processed URL by updating its frequency in the frontier or adding it if it doesn't exist.
     * @param url The URL to handle
     * @return true if the URL was newly added, false if it already existed and was updated
     */
    public boolean handleUrl(String url) {
        return urlsFrontierService.upsertUrl(url, MAX_URLS);
    }

    /**
     * Checks if the frontier should be initialized.
     *
     * @return true if the frontier is empty, false otherwise
     */
    public boolean shouldInitializeFrontier() {
        return urlsFrontierService.isEmpty();
    }

    /**
     * Updates a normalized URL's document after being crawled in the database.
     * @param normalizedUrl the crawled url.
     * @param document URL's page content.
     * @param hashedContent URL's page content hash.
     * @param linkedPages URL's linked pages.
     */
    public void saveCrawledDocument(
        String normalizedUrl,
        byte[] document,
        String hashedContent,
        List<String> linkedPages
    ) {
        UrlDocument urlDocument = new UrlDocument(
            normalizedUrl,
            1,
            true,
            document,
            hashedContent,
            linkedPages,
            new Date().toString()
        ); // dummy frequency
        urlsFrontierService.updateUrlDocument(urlDocument);
    }

    /**
     * Checks if the URL frontier has reached its threshold.
     *
     * @return true if the threshold is reached, false otherwise.
     */
    public boolean hasReachedThreshold() {
        return urlsFrontierService.count() >= MAX_URLS;
    }

    /**
     * Removes a URL from the frontier if it proves to be un-crawlable.
     * @param normalizedUrl the URL to be removed.
     */
    public void removeUrl(String normalizedUrl) {
        urlsFrontierService.deleteByNormalizedUrl(normalizedUrl);
    }

    /**
     * Checks if a page content is already in the frontier by checking the hash cache.
     *
     * @param normalizedUrl The normalized URL to check.
     * @return true if the URL exists, false otherwise.
     */
    public boolean isDuplicate(String hashedDocContent) {
        return !hashedDocsCache.add(hashedDocContent);
    }
}
