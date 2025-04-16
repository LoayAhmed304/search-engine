package com.project.searchengine.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Crawler {

    private final UrlsFrontier urlsFrontier;
    private static int currentBatch = 1;

    @Autowired
    public Crawler(UrlsFrontier urlsFrontier) {
        this.urlsFrontier = urlsFrontier;
    }

    public void initCrawling() {
        if(urlsFrontier.shouldInitializeFrontier())
            urlsFrontier.seedFrontier();
    }

    /**
     * Starts the crawling process.
     */
    public void crawl() {
        System.out.println("Starting the crawling process...");
        initCrawling();
        while(urlsFrontier.getNextUrlsBatch())
        {
            System.out.println("Processing batch of URLs number: " + currentBatch++);

            for (String url : urlsFrontier.currentUrlBatch) {
                System.out.println("Crawling URL: " + url);
                // rest of crawling logic goes here
                
            }
        }
        System.out.println("Finished processing totoal batch of URLs of count: " + (currentBatch - 1));
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
}