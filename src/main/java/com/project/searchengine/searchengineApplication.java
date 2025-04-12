package com.project.searchengine;

import com.project.searchengine.indexer.DocumentPreprocessor;
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
        DocumentPreprocessor dp = context.getBean(DocumentPreprocessor.class);
        String url =
            "https://medium.com/@shwetkhatri/my-journey-in-lfx-mentorship-program-summer22-39d861c97313";
        try {
            Document document = Jsoup.connect(url).get();
            dp.preprocessDocument(url, document);
            System.out.println("Indexed URL: " + url);
        } catch (Exception e) {
            System.err.println("Error indexing URL: " + url + " - " + e.getMessage());
        }
    }
}
