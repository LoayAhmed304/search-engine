package com.project.searchengine.indexer;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class DocumentPreprocessor {
    
    public Map<String, Object> preprocessDocument(String url, Document document) {
        Map<String, Object> pageData = new HashMap<>();
        Map<String, List<String>> headers = new HashMap<>(); // word => header type
        String title = document.title();
        String content = document.body().text();

        // 1- Extract headers and tokenize them
        headers = extractHeaders(document);
        
        // 2- Convert to lower case
        String lowerContent = content.toLowerCase();

        // 3- Remove punctuation and special characters
        String cleanContent = lowerContent.replaceAll("[^\\w\\s]", "");

        // 4- Tokenize the content


        // 5- Remove stop words

        // 6- Stemming

        // 7- Remove numbers

        // 8- Filter short tokens



        return pageData;
    } 

    public Map<String, List<String>> extractHeaders(Document document) {
        Map<String, List<String>> headers = new HashMap<>();
        Elements headerTags = document.select("h1, h2, h3, h4, h5, h6");
        
        for (Element header: headerTags) {
            String headerText = header.text();
            String headerType = header.tagName();
            String[] headerTokens = headerText.toLowerCase().split("[,\\s\\.\\?\\!\\-\\(\\)]+");
            headers.computeIfAbsent(headerType, k -> new ArrayList<>()).addAll(Arrays.asList(headerTokens));
        }
        
        return headers;
    }

    public static void main(String[] args) {
        DocumentPreprocessor dp = new DocumentPreprocessor();
        String url = "https://www.geeksforgeeks.org/basics-computer-networking/";

        try {
            Document document = Jsoup.connect(url).get();

            Map<String, Object> pageData = new HashMap<>();
            pageData = dp.preprocessDocument(url, document);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
   
    }
}
