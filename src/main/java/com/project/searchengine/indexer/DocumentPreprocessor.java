package com.project.searchengine.indexer;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class DocumentPreprocessor {

    Tokenizer tokenizer = new Tokenizer();

    /**
     * Preprocesses the document by extracting tokens and their positions.
     * It also extracts header tokens and their occurrences.
     * @param url The URL of the document.
     * @param document The Jsoup Document object.
     * @return A map containing the tokens and their positions, as well as header tokens. (To be changed)
     */
    public Map<String, Object> preprocessDocument(String url, Document document) {
        Map<String, Object> pageData = new HashMap<>();

        // Field-specefic tokens
        Map<String, List<Integer>> bodyTokens = new HashMap<>(); // word => list of positions
        Map<String, Map<String, Integer>> headerTokens = new HashMap<>(); // word => header => count of occurrences

        // Extract raw text
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6, title");

        // Tokenize the document
        bodyTokens = tokenizer.tokenize(content);
        headerTokens = tokenizer.tokenizeHeaders(fieldTags);

        return pageData;
    }

    // Main method for testing
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
