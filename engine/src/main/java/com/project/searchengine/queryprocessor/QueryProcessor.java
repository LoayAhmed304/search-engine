package com.project.searchengine.queryprocessor;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.ranker.Ranker;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.InvertedIndexService;

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
    private Ranker ranker;

    private final int threadsNum = 20;

    private int pageSize = 20;

    private Integer resultPagesNumber = 0;
    private final Map<String, String> allPagesSnippets = new ConcurrentHashMap<>();
    private List<Map<PageReference, String>> rankedPageBatches;

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
            resultPagesNumber += tokenPages.size();
        }
        return queryPages;
    }

    /**
     * Retrieves the number of result pages for the processed query
     *
     * @return The number of result pages.
     */
    public Integer getResultPagesNumber() {
        return resultPagesNumber;
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
    private Map<String, String> getBatchSnippets(String query, int pageNumber) {

        // check if the page number is valid
        if (rankedPageBatches == null || rankedPageBatches.isEmpty()) {
            System.out.println("No pages found for the query: " + query);
            return allPagesSnippets;
        }

        if(pageNumber >= rankedPageBatches.size()) {
            System.out.println("Invalid page number: " + pageNumber);
            return allPagesSnippets;
        }
        // based on page number get map
        Map<PageReference, String> rankedPages = rankedPageBatches.get(pageNumber);

        ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);

        QueryResult queryResult = queryTokenizer.tokenizeQuery(query);
        List<Future<Map<String, String>>> futures = new ArrayList<>();

        for (Map.Entry<PageReference, String> entry : rankedPages.entrySet()) {
            PageReference page = entry.getKey();
            String token = entry.getValue();

            Future<Map<String, String>> future = executorService
                    .submit(() -> snippetGenerator.getPagesSnippets(token, page, queryResult));

            futures.add(future);
        }

        for (Future<Map<String, String>> future : futures) {
            try {
                Map<String, String> batchSnippets = future.get();
                allPagesSnippets.putAll(batchSnippets);
            } catch (Exception e) {
                System.err.println("Error in processing token page: " + e.getMessage());
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            e.printStackTrace();
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
    private void displaySnippets(Map<String, String> pageSnippet) {
        int i = 1;
        for (Map.Entry<String, String> entry : pageSnippet.entrySet()) {
            String pageID = entry.getKey();
            String snippet = entry.getValue();

            System.out.println("Page no: " + i++ + ":");
            System.out.println("Page: " + pageID);
            System.out.println("-> Snippet: " + snippet);

            System.out.println("--------------------------------------------------");
        }
    }

    /**
     * Splits a given map into a list of smaller maps
     *
     * @param originalMap The original map to be split
     * @param size        Size of each partiition
     * @return A list of smaller maps, where each map contains at most {@code size}
     *         entries
     */
    public static List<Map<PageReference, String>> splitMap(Map<PageReference, String> originalMap, int size) {
        List<Map<PageReference, String>> result = new ArrayList<>();
        List<Map.Entry<PageReference, String>> entryList = new ArrayList<>(originalMap.entrySet());

        for (int i = 0; i < entryList.size(); i += size) {
            int end = Math.min(i + size, entryList.size());
            Map<PageReference, String> batchMap = new LinkedHashMap<>();

            for (int j = i; j < end; j++) {
                Map.Entry<PageReference, String> entry = entryList.get(j);
                batchMap.put(entry.getKey(), entry.getValue());
            }
            result.add(batchMap);
            System.out.println("Batch size: " + batchMap.size());
        }

        return result;
    }

    /**
     * Processes the given query by tokenizing it, retrieving pages for each token,
     * and generating snippets for the relevant pages.
     *
     * @param query The search query to process.
     */
    public void process(String query, int pageNumber) {
        // Reset result pages number before processing a new query
        resultPagesNumber = 0;

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
            System.out.println("phrase match query");

            // filter the pages based on the phrase match first
            queryPages = phraseMatcher.filterPhraseMatchPages(queryPages, queryResult);
            // update pages number
            resultPagesNumber = 0;
            for (Map.Entry<String, List<PageReference>> entry : queryPages.entrySet()) {
                List<PageReference> pages = entry.getValue();
                resultPagesNumber += pages.size();
            }
        }

        Map<PageReference, String> rankedPages = ranker.rank(queryPages);
        this.rankedPageBatches = splitMap(rankedPages, pageSize);

        getBatchSnippets(query, pageNumber);
    }

    /**
     * Returns all the snippets generated for the pages.
     *
     * @return A map where the key is a page reference, and the value is the snippet
     *         for that page.
     */
    public Map<String, String> getAllPagesSnippets(String query, int pageNumber) {
        allPagesSnippets.clear();
        process(query, pageNumber);
        return allPagesSnippets;
    }

    /**
     * Returns the total number of result pages based on the last processed query.
     * This represents the actual number of page batches after ranking and
     * filtering.
     *
     * @return The total number of page batches available.
     */
    public int getTotalPages() {
        return resultPagesNumber;
    }
}
