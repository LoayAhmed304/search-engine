package com.project.searchengine.indexer;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class DocumentPreprocessor {

    Tokenizer tokenizer = new Tokenizer();

    public Map<String, Object> preprocessDocument(String url, Document document) {
        Map<String, Object> pageData = new HashMap<>();

        // Field-specefic tokens
        Map<String, List<Integer>> bodyTokens = new HashMap<>(); // word => list of positions
        Map<String, List<Integer>> titleTokens = new HashMap<>();
        Map<String, Map<String, Integer>> headerTokens = new HashMap<>(); // word => header => count of occurrences

        Map<String, Integer> fields = new HashMap<>(); // field name -> word count

        // Extract raw text
        String title = document.title();
        String content = document.body().text();

        bodyTokens = tokenizer.tokenize(content);
        titleTokens = tokenizer.tokenize(title);
        extractHeaders(document);

        return pageData;
    }

    public Map<String, Map<String, Integer>> extractHeaders(Document document) {
        Map<String, List<String>> headers = new HashMap<>();
        Map<String, Integer> fieldCounts = new HashMap<>();
        Map<String, Map<String, Integer>> headerTokens = new HashMap<>(); // token => header type => count

        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6");

        for (Element header : fieldTags) {
            String headerText = header.text();
            String headerType = header.tagName();
            Map<String, List<Integer>> tokens = tokenizer.tokenize(headerText);

            // check if the token exits in the map
            for (Map.Entry<String, List<Integer>> entry : tokens.entrySet()) {
                String token = entry.getKey();
                Integer tokenCount = entry.getValue().size();

                // check if the token exist in the map
                if (headerTokens.containsKey(token)) {
                    Map<String, Integer> headerTypeCount = headerTokens.get(token);
                    if (headerTypeCount.containsKey(headerType)) {
                        // update the count
                        Integer count = headerTypeCount.get(headerType);
                        headerTypeCount.put(headerType, count + tokenCount);
                    } else {
                        headerTypeCount.put(headerType, tokenCount);
                    }
                } else {
                    // add the token to the map
                    Map<String, Integer> headerTypeCount = new HashMap<>();
                    headerTypeCount.put(headerType, tokenCount);
                    headerTokens.put(token, headerTypeCount);
                }
            }
        }
        System.out.println("Headers: " + headerTokens);
        return headerTokens;
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
