package com.project.searchengine;

import com.project.searchengine.indexer.DocumentPreprocessor;
import com.project.searchengine.indexer.Tokenizer;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class searchengineApplication {

    public static void main(String[] args) {
        //  SpringApplication.run(searchengineApplication.class, args);
        System.out.println("Hello my Search Engine!");
        ApplicationContext context = SpringApplication.run(searchengineApplication.class, args);

        // Test the indexer
        Document document = null;
        DocumentPreprocessor dp = context.getBean(DocumentPreprocessor.class);
        String url = "https://gustavus.edu/academics/departments/english/whystudyliterature.php";
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
