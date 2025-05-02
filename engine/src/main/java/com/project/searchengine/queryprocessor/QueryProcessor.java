package com.project.searchengine.queryprocessor;

import java.util.*;
import java.util.concurrent.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.PageRepository;
import com.project.searchengine.server.service.InvertedIndexService;

import opennlp.tools.tokenize.SimpleTokenizer;

@Component
public class QueryProcessor {

    @Autowired
    private InvertedIndexService invertedIndexService;

    @Autowired
    private QueryTokenizer queryTokenizer;

    @Autowired
    private SnippetGenerator snippetGenerator;

    @Autowired
    private PhraseMatcher phraseMatcher;

    @Autowired
    private PageRepository pageRepository;

    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    private final int batchSize = 2;
    private final int threadsNum = 20;

    Map<String, String[]> pageTokensCache = new HashMap<>(); // map of pages ids with its body tokens
    private Map<PageReference, String> allPagesSnippets = new HashMap<>(); // map of page id with its snippet

    /**
     * Retrieves the result pages for each token in the processed query
     *
     * @param tokenizedQuery A list of tokens from the search query
     * @return A map where the key is the token, and the value is a list of pages
     *         containing that token.
     */
    public Map<String, List<PageReference>> retrieveQueryPages(List<String> tokenizedQuery) {
        Map<String, List<PageReference>> queryPages = new HashMap<>();

        for (String token : tokenizedQuery) {
            List<PageReference> tokenPages = invertedIndexService.getTokenPages(token);
            queryPages.put(token, tokenPages);
        }
        return queryPages;
    }

    /**
     * Gets the tokenized body contnet of reference page provided
     * 
     * @param referencePage
     * @return body content tokens
     */
    public String[] getPageBodyContent(PageReference referencePage) {

        // get the tokenized body content
        String pageId = referencePage.getPageId();
        Page page = pageRepository.getPageById(pageId);

        String content = page.getContent();
        Document document = Jsoup.parse(content);
        String bodyContent = document.body().text();

        return tokenizer.tokenize(bodyContent.toLowerCase());
    }

    /**
     * Retrieves snippets for a batch of pages based on the query and token
     *
     * @param query The original search query
     * @param token The token for which snippets are being retrieved
     * @param pages A list of pages to retrieve snippets for
     * @return A map where the key is a page reference, and the value is the snippet
     *         for that page.
     */
    private Map<PageReference, String> getBatchSnippets(String query, String token, List<PageReference> pages) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);

        QueryResult queryResult = queryTokenizer.tokenizeQuery(query);
        List<Future<Map<PageReference, String>>> futures = new ArrayList<>();

        for (int start = 0; start < pages.size(); start += batchSize) {

            int end = Math.min(start + batchSize, pages.size());
            List<PageReference> batch = pages.subList(start, end);

            Future<Map<PageReference, String>> future = executorService
                    .submit(() -> snippetGenerator.getPagesSnippets(token, batch, queryResult));

            futures.add(future);
        }

        for (Future<Map<PageReference, String>> future : futures) {
            try {
                Map<PageReference, String> batchSnippets = future.get();
                allPagesSnippets.putAll(batchSnippets);
            } catch (Exception e) {
                System.err.println("Error in processing token page: " + e.getMessage());
            }
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        displaySnippets(allPagesSnippets);
        return allPagesSnippets;
    }

    /**
     * Helper function to displays snippets for the given pages and their associated
     * snippets
     *
     * @param pageSnippet A map where the key is a page reference, and the value is
     *                    the snippet for that page
     */
    private void displaySnippets(Map<PageReference, String> pageSnippet) {
        for (Map.Entry<PageReference, String> entry : pageSnippet.entrySet()) {
            PageReference page = entry.getKey();
            String snippet = entry.getValue();

            System.out.println("Page: " + page.getPageId());
            System.out.println("-> Snippet: " + snippet);

            System.out.println("--------------------------------------------------");
        }
    }

    /**
     * Processes the given query by tokenizing it, retrieving pages for each token,
     * and generating snippets for the relevant pages.
     *
     * @param query The search query to process.
     */
    public void process(String query) {

        QueryResult queryResult = queryTokenizer.tokenizeQuery(query);
        List<String> tokenizedQuery = queryResult.getTokenizedQuery();
        Map<String, List<PageReference>> queryPages = retrieveQueryPages(tokenizedQuery);

        for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
            String token = entry.getKey();
            List<PageReference> tokenPages = entry.getValue();

            System.out.println("Token: " + token + " | Pages Found: " + (tokenPages != null ? tokenPages.size() : 0));
        }

        // phrase matching
        boolean isPhraseMatch = phraseMatcher.isPhraseMatchQuery(query);

        if (isPhraseMatch) {
            // filter the pages based on the phrase match
            queryPages = phraseMatcher.filterPhraseMatchPages(queryPages, queryResult);

            for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
                String token = entry.getKey();
                List<PageReference> tokenPages = entry.getValue();
                System.out.println("token: " + token + " Filtered Pages: " + tokenPages.size());
            }

            // get the page snippets
            for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
                String token = entry.getKey();
                List<PageReference> tokenPages = entry.getValue();
                // get first 20 pages
                tokenPages = tokenPages.subList(0, 20);
                getBatchSnippets(query, token, tokenPages);
                break;
            }
        }

        if (!isPhraseMatch) {
            for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
                String token = entry.getKey();
                List<PageReference> tokenPages = entry.getValue();
                tokenPages = tokenPages.subList(0, 20);
                getBatchSnippets(query, token, tokenPages);
                break;
            }
        }
    }

    /**
     * Returns all the snippets generated for the pages.
     *
     * @return A map where the key is a page reference, and the value is the snippet
     *         for that page.
     */
    public Map<PageReference, String> getAllPagesSnippets(String query) {
        allPagesSnippets.clear();
        process(query);
        return allPagesSnippets;
    }

}
