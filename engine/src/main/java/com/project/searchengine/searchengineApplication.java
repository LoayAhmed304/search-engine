package com.project.searchengine;

// import com.project.searchengine.crawler.Crawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application context
        SpringApplication.run(searchengineApplication.class, args);
        // ApplicationContext context = SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");
        // Test the crawler by seeding the frontier
        // Crawler crawler = context.getBean(Crawler.class);
        // long start = System.currentTimeMillis();
        // // crawler.seed();
        // crawler.crawl();
        // long duration = System.currentTimeMillis() - start;
        // System.out.println("retrieving from the frontier took: " + duration + " ms");
    }
}
