package com.project.searchengine.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import org.jsoup.nodes.*;

import com.project.searchengine.crawler.preprocessing.URLExtractor;
import com.project.searchengine.crawler.preprocessing.URLNormalizer;
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
     * Handles the initialization of the crawling process.
     * It determines whether the seeding of the frontier is necessary.
     */
    public void initCrawling() {
        if(urlsFrontier.shouldInitializeFrontier())
            urlsFrontier.seedFrontier();
    }

    /**
     * Includes the crawling process skeleton.
     */
    public void crawl() {
        System.out.println("Starting the crawling process...");
        initCrawling();
    
        while(urlsFrontier.getNextUrlsBatch())
        {
            System.out.println("Processing batch of URLs number: " + currentBatch++);

            for (String url : urlsFrontier.currentUrlBatch) {
                System.out.println("Crawling URL: " + url);

                Document pageContent = URLExtractor.getDocument(url);

                String hashedDocument = HashManager.hash(pageContent.toString()); 
                System.out.println("Hashed Document: " + hashedDocument);
                   // if the hash is in the database, remove the url from the frontier db urlsFrontier.removeDuplicates(hashedDocument);

                Set<String> linkedPagesSet = URLExtractor.getURLs(pageContent);
                List<String> linkedPages = new ArrayList<>(linkedPagesSet);
                System.out.println("Linked Pages: " + linkedPages.size());
                for (String linkedUrl : linkedPages) {

                    String normalizedUrl = URLNormalizer.normalizeUrl(linkedUrl);
                    if(!robotsHandler.isUrlAllowed(url))
                        continue;
                    
                    urlsFrontier.handleUrl(normalizedUrl);
                }
                
                // 3. update the document in the database
                urlsFrontier.saveCrawledDocument(url, pageContent.toString(), hashedDocument, true, linkedPages);
        }
        System.out.println("Finished processing totoal batch of URLs of count: " + (currentBatch - 1));
        System.out.println("Crawling process completed.");
    }

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