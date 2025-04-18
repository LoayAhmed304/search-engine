package com.project.searchengine;

import com.project.searchengine.indexer.Indexer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
// import com.project.searchengine.crawler.Crawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application context
        // SpringApplication.run(searchengineApplication.class, args);
        ApplicationContext context = SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");

        // Test the crawler by seeding the frontier
        // Crawler crawler = context.getBean(Crawler.class);
        // long start = System.currentTimeMillis();
        // // crawler.seed();
        // crawler.crawl();
        // long duration = System.currentTimeMillis() - start;
        // System.out.println("retrieving from the frontier took: " + duration + " ms");

        // Test the indexer
        Document document = null;
        Indexer dp = context.getBean(Indexer.class);

        String url = "https://en.wikipedia.org/wiki/Computer_program";
        try {
            document = Jsoup.connect(url).get();

            System.out.println("Connected to URL: " + url);
        } catch (Exception e) {
            System.err.println("Error indexing URL: " + url + " - " + e.getMessage());
        }

        long start = System.currentTimeMillis();
        dp.preprocessDocument(url, document);
        long duration = System.currentTimeMillis() - start;
        System.out.println("Indexing took: " + duration + " ms");
    }
}
