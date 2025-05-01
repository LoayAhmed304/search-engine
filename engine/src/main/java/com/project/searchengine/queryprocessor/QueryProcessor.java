package com.project.searchengine.queryprocessor;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.QueryService;

@Component
public class QueryProcessor {
    private final QueryService queryService;

    @Autowired
    private QueryTokenizer queryTokenizer;

    @Autowired
    private SnippetGenerator snippetGenerator;

    @Autowired
    private PhraseMatcher phraseMatcher;

    private final int batchSize = 2;
    private final int threadsNum = 20;

    public QueryProcessor(QueryService queryService) {
        this.queryService = queryService;
    }

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
            List<PageReference> tokenPages = queryService.getTokenPages(token);
            queryPages.put(token, tokenPages);
        }
        return queryPages;
    }

    /**
     * Finds the token with the minimum number of associated pages
     *
     * @param queryPages A map where the key is the token, and the value is a list
     *                   of pages containing that token
     * @return The token with the fewest associated pages
     */
    private String getMinPagesToken(Map<String, List<PageReference>> queryPages) {
        String minToken = "";
        int minPagesNumber = Integer.MAX_VALUE;

        for (String token : queryPages.keySet()) {
            List<PageReference> pages = queryPages.get(token);
            if (pages.size() < minPagesNumber) {
                minToken = token;
                minPagesNumber = pages.size();
            }
        }

        return minToken;
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

        QueryTokenizationResult queryTokenizationResult = queryTokenizer.tokenizeQuery(query);
        List<Future<Map<PageReference, String>>> futures = new ArrayList<>();

        for (int start = 0; start < pages.size(); start += batchSize) {

            int end = Math.min(start + batchSize, pages.size());
            List<PageReference> batch = pages.subList(start, end);

            Future<Map<PageReference, String>> future = executorService
                    .submit(() -> snippetGenerator.getPagesSnippets(token, batch, queryTokenizationResult));

            futures.add(future);
        }

        Map<PageReference, String> allSnippets = new HashMap<>();

        for (Future<Map<PageReference, String>> future : futures) {
            try {
                Map<PageReference, String> batchSnippets = future.get();
                allSnippets.putAll(batchSnippets);
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

        // displaySnippets(allSnippets);
        return allSnippets;
    }

    /**
     * Processes the given query by tokenizing it, retrieving pages for each token,
     * and generating snippets for the relevant pages.
     *
     * @param query The search query to process.
     */
    public void process(String query) {

        QueryTokenizationResult queryTokenizationResult = queryTokenizer.tokenizeQuery(query);
        List<String> tokenizedQuery = queryTokenizationResult.getTokenizedQuery();
        Map<String, List<PageReference>> queryPages = retrieveQueryPages(tokenizedQuery);

        for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
            String token = entry.getKey();
            List<PageReference> tokenPages = entry.getValue();

            System.out.println("Token: " + token + " | Pages Found: " + (tokenPages != null ? tokenPages.size() : 0));
        }

        // phrase matching
        boolean isPhraseMatch = phraseMatcher.isPhraseMatchQuery(query);

        if (isPhraseMatch) {
            // get token which has min number of pages first
            String minPagesToken = getMinPagesToken(queryPages);
            List<PageReference> minPages = queryPages.get(minPagesToken);
            minPages.subList(0, 20);
            getBatchSnippets(query, minPagesToken, minPages);
        }

        if (!isPhraseMatch) {
            for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
                String token = entry.getKey();
                List<PageReference> tokenPages = entry.getValue();
                // tokenPages = tokenPages.subList(0, 20);
                getBatchSnippets(query, token, tokenPages);
                break;
            }
        }
    }
}
