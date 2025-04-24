package com.project.searchengine.runner;
import com.project.searchengine.crawler.Crawler;

import java.util.ArrayList;
import java.util.List;

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
        List<Thread> threads = new ArrayList<>();
        int numThreads = 30; // User-defined thread count

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(crawler); // Shared Crawler instance
            threads.add(t);
            t.start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
        System.out.println("Crawling took: " + (System.currentTimeMillis() - start) + "ms");
    }
}