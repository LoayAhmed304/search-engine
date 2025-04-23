package com.project.searchengine.crawler;


import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.service.UrlsFrontierService;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlsFrontier {
    private UrlsFrontierService urlsFrontierService;

    private final String SEEDS_FILE_PATH = Paths.get("src/main/resources/seeds.txt").toString();
    public static final int BATCH_SIZE = 200;
    public static final int MAX_URLS = 1000;
    public List<String> currentUrlBatch = new ArrayList<>();
    public HashSet<String> allHashedDocs = new HashSet<>();

    /**
     * Constructor for UrlsFrontier.
     *
     * @param urlsFrontierService Service to manage URLs in the frontier
     */
    @Autowired
    public UrlsFrontier(UrlsFrontierService urlsFrontierService) {
        this.urlsFrontierService = urlsFrontierService;
    }
    /**
     * Initializes the frontier with a list of seed URLs.
     *
     * @param seedUrls List of seed URLs to initialize the frontier
     */
    public void seedFrontier() {
        List<String> seedUrls = readSeeds();
        System.out.println("Seeding the frontier with " + seedUrls.size() + " URLs." + seedUrls);
        urlsFrontierService.initializeFrontier(seedUrls);
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
     * @return true if the batch was successfully retrieved, false if no more URLs are available
     */
    public boolean getNextUrlsBatch() {
        List<String> urls = urlsFrontierService.getTop100UrlsByFrequency();
        if (urls.isEmpty()) {
            return false;
        }
        // replace the current batch with the new batch
        currentUrlBatch.clear();
        for (int i = 0; i < urls.size(); i++)
            currentUrlBatch.add(urls.get(i));

        return true;
    }

    public void getAllHashedDocContent() {
        allHashedDocs.addAll(urlsFrontierService.findAllHashedDocContent());
    }

    /**
     * Handles processed URL by updating its frequency in the frontier or adding it if it doesn't exist.
     * @param url The URL to handle
     * @return true if the URL was newly added, false if it already existed and was updated
     */
    public boolean handleUrl(String url) {
        return urlsFrontierService.upsertUrl(url);
    }

    /**
     * Checks if the frontier should be initialized.
     *
     * @return true if the frontier is empty, false otherwise
     */
    public boolean shouldInitializeFrontier() {
        return urlsFrontierService.isEmpty();
    }

    public void saveCrawledDocument(String normalizedUrl, String document, String hashedContent, boolean isCrawled ,List<String> linkedPages) {
        UrlDocument urlDocument = new UrlDocument(normalizedUrl, 1, isCrawled, document, hashedContent, linkedPages, new Date().toString()); // dummy frequency
        urlsFrontierService.updateUrlDocument(urlDocument);
    }

    public boolean hasReachedThreshold() {
        return urlsFrontierService.count() >= MAX_URLS;
    }

    public void removeUrl(String normalizedUrl) {
        urlsFrontierService.deleteByNormalizedUrl(normalizedUrl);
    }
    public boolean isDuplicate(String hashedDocContent) {
        return !allHashedDocs.add(hashedDocContent);
    }
    
}
