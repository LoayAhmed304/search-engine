package com.project.searchengine.crawler;

import com.project.searchengine.crawler.preprocessing.URLExtractor;
import com.project.searchengine.crawler.preprocessing.URLNormalizer;
import com.project.searchengine.utils.CompressionUtil;
import com.project.searchengine.utils.HashManager;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class Crawler {

    private final UrlsFrontier urlsFrontier;
    private RobotsHandler robotsHandler;
    private static int currentBatch = 1;
    private int numThreads; // Field to store the number of threads

    @Autowired
    public Crawler(UrlsFrontier urlsFrontier) {
        this.urlsFrontier = urlsFrontier;
        this.robotsHandler = new RobotsHandler();
    }

    // Setter for numThreads
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    /**
     * Handles the initialization of the crawling process: seeding, preparing caches, etc.
     */
    public void initCrawling() {
        if (urlsFrontier.shouldInitializeFrontier()) seed();
        urlsFrontier.getAllHashedDocContent(); // Prepare the cache
    }

    /**
     * Crawls the URLs managed by the frontier using multiple threads.
     * It processes each URL in the current batch concurrently, fetches its content,
     * checks for duplicates, and handles linked pages.
     */
    public void crawl() {
        System.out.println("Starting the crawling process...");
        initCrawling();

        // Validate numThreads
        if (numThreads <= 0) {
            System.err.println("Invalid number of threads: " + numThreads + ". Defaulting to 1 thread.");
            numThreads = 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        while (urlsFrontier.getNextUrlsBatch()) {
            System.out.println("\nProcessing batch of URLs number: " + currentBatch++);
            System.out.println("Current batch size: " + urlsFrontier.currentUrlBatch.size());

            List<Future<?>> futures = new ArrayList<>();
            for (String url : urlsFrontier.currentUrlBatch) {
                futures.add(executor.submit(() -> processUrl(url)));
            }

            // Wait for all tasks in the batch to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    System.err.println("Error processing URL: " + e.getMessage());
                }
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("Finished processing total batch of URLs of count: " + (currentBatch - 1));
        System.out.println("Crawling process completed.");
    }

    private void processUrl(String url) {
        System.out.println("Crawling URL: " + url + " on thread " + Thread.currentThread().getName());

        Document pageContent = URLExtractor.getDocument(url);
        String stringifiedPage = pageContent != null ? pageContent.toString() : null;

        if (pageContent == null) {
            System.out.println("Failed to fetch content for URL: " + url);
            urlsFrontier.removeUrl(url);
            return;
        }

        String hashedDocument = HashManager.hash(stringifiedPage);

        if (urlsFrontier.isDuplicate(hashedDocument)) {
            System.out.println("Duplicate document found. Skipping URL: " + url);
            urlsFrontier.removeUrl(url);
            return;
        }

        List<String> linkedPages = new ArrayList<>(URLExtractor.getURLs(pageContent));

        handleLinkedPages(linkedPages);

        urlsFrontier.saveCrawledDocument(
            url,
            CompressionUtil.compress(stringifiedPage),
            hashedDocument,
            linkedPages
        );
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
     * It checks if each linked page is allowed to be crawled based on robots.txt rules.
     *
     * @param linkedPages List of linked pages to handle
     */
    private void handleLinkedPages(List<String> linkedPages) {
        if (!urlsFrontier.hasReachedThreshold()) {
            for (String linkedUrl : linkedPages) {
                String normalizedUrl = URLNormalizer.normalizeUrl(linkedUrl);

                if (!robotsHandler.isUrlAllowed(normalizedUrl)) continue;

                if (!urlsFrontier.handleUrl(normalizedUrl)) break;
            }
        }
    }

    public void test() {
        crawl();
    }
}