package com.project.searchengine.runner;
import com.project.searchengine.crawler.Crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("crawler")
public class CrawlerRunner implements CommandLineRunner {

    @Autowired
    private Crawler crawler;

    @Override
    public void run(String... args) {
        System.out.println("Starting the crawler...");
        long start = System.currentTimeMillis();
        int numThreads = 5; // User-defined thread count

        crawler.setNumThreads(numThreads);
        crawler.crawl();
        System.out.println("Crawling took: " + (System.currentTimeMillis() - start)/60000 + " minutes");
    }
}