package com.project.searchengine.indexer;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class DocumentPreprocessor {

    Tokenizer tokenizer = new Tokenizer();

    public Map<String, Object> preprocessDocument(String url, Document document) {
        Map<String, Object> pageData = new HashMap<>();
        Map<String, List<Integer>> tokens = new HashMap<>();
        Map<String, List<String>> headers = new HashMap<>(); // word => header type
        String title = document.title();
        String content = document.body().text();
        headers = extractHeaders(document);

        tokens = tokenizer.tokenize(content + title);

        return pageData;
    }

    public Map<String, List<String>> extractHeaders(Document document) {
        Map<String, List<String>> headers = new HashMap<>();
        Elements headerTags = document.select("h1, h2, h3, h4, h5, h6, title");

        for (Element header : headerTags) {
            String headerText = header.text();
            String headerType = header.tagName();
            String[] headerTokens = headerText.toLowerCase().split("[,\\s\\.\\?\\!\\-\\(\\)]+");
            headers
                .computeIfAbsent(headerType, k -> new ArrayList<>())
                .addAll(Arrays.asList(headerTokens));
        }
        System.out.println("Headers: " + headers);
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
