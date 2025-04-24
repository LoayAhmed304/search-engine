package com.project.searchengine;

import com.project.searchengine.crawler.Crawler;
import com.project.searchengine.indexer.Indexer;
import com.project.searchengine.ranker.PageRank;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
import opennlp.tools.dictionary.Index;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application context
        SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");
    }
}
