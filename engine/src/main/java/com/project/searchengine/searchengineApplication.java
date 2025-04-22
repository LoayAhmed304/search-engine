package com.project.searchengine;

import com.project.searchengine.ranker.PageRank;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
// import com.project.searchengine.crawler.Crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application context
        ApplicationContext context = SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");
        UrlsFrontierService urlsFrontierService = context.getBean(UrlsFrontierService.class);
        PageService pageService = context.getBean(PageService.class);

        System.out.println(
            "\n\n\n\n\t\t***************Starting PageRank calculation***************"
        );
        long startTime = System.currentTimeMillis();
        PageRank pageRank = new PageRank(urlsFrontierService, pageService);
        boolean success = pageRank.computeAllRanks();
        long duration = System.currentTimeMillis() - startTime;

        if (success) {
            System.out.println("PageRank calculation completed successfully!");
            System.out.println("Pages ranked: " + urlsFrontierService.getAllUrls().size());
            System.out.println("Time taken: " + duration + "ms\n\n");
        } else {
            System.out.println("PageRank calculation failed!\n\n");
        }
        // Test the crawler by seeding the frontier
        // Crawler crawler = context.getBean(Crawler.class);
        // long start = System.currentTimeMillis();
        // crawler.crawl();
        // long duration = System.currentTimeMillis() - start;
        // System.out.println("crawling took: " + duration + " ms");
    }
}
