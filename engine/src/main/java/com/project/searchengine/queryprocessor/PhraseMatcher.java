package com.project.searchengine.queryprocessor;

import java.util.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageReferenceService;

import opennlp.tools.tokenize.SimpleTokenizer;

@Component
public class PhraseMatcher {

    @Autowired
    private PageReferenceService pageReferenceService;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    private final int threadsNum = 40; 
    private final Map<String, Integer> matchPositions = new ConcurrentHashMap<>(); // store match positions for each
                                                                                   // page when filtering

    /**
     * @param query
     * @return whether it is a phrase matching query or not
     */
    public boolean isPhraseMatchQuery(String query) {
        return query.matches("^\".{1,}\"$");
    }

    /**
     * @param tokenIndex
     * @param querySize  length of query
     * @return whether the tokenIndex is a valid index or not
     */
    private boolean isValidTokenIndex(int tokenIndex, int querySize) {
        return tokenIndex >= 0 && tokenIndex < querySize;
    }

    /**
     * @param tokenIndex
     * @param bodyTokensLength
     * @return whether the offset is a valid index or not
     */
    private boolean isValidOffset(int offset, int bodyTokensLength) {
        return offset >= 0 && offset < bodyTokensLength;
    }

    /**
     * Retrieves the match position for a given page ID.
     *
     * @param pageId the unique identifier of the page
     * @return the position of the match for the specified page ID
     */
    public Integer getMatchPosition(String pageId) {
        return matchPositions.get(pageId);
    }

    /**
     * Find if the words before or after a specific token (depending on
     * isBeforeToken) match or not
     * 
     * @param bodyTokens
     * @param originalWords list of the query original words before tokenization
     * @param tokenIndex
     * @param isBeforeToken to determine if the match is before the anchor token or
     *                      after it
     * @return true if a match is found
     */
    private boolean findMatchAroundToken(String[] bodyTokens,
            List<String> originalWords,
            int tokenIndex, int pos, boolean isBeforeToken) {

        boolean found = true;
        int currentOffset = isBeforeToken ? pos - 1 : pos + 1;
        int currentTokenIndex = isBeforeToken ? tokenIndex - 1 : tokenIndex + 1;
        int querySize = originalWords.size();

        while (found &&
                isValidOffset(currentOffset, bodyTokens.length)
                && isValidTokenIndex(currentTokenIndex, querySize)) {

            String currentToken = originalWords.get(currentTokenIndex);

            if (!currentToken.toLowerCase().equals(bodyTokens[currentOffset])) {
                found = false;
            }

            currentOffset = isBeforeToken ? currentOffset - 1 : currentOffset + 1;
            currentTokenIndex = isBeforeToken ? currentTokenIndex - 1 : currentTokenIndex + 1;
        }

        return found;
    }

    /**
     * Checks if a given token is part of a phrase match in the body content or not
     *
     * @param bodyTokens:
     * @param originalWords: original query words
     * @param token:         token to match
     * @param pos:           position of the current token in body content
     * @return true if the token is part of a matching phrase false otherwise.
     */
    public boolean isPhraseMatchFound(String[] bodyTokens,
            List<String> originalWords, String token, int pos) {

        int tokenIndex = originalWords.indexOf(token);

        if (tokenIndex == -1)
            return false;

        // check current
        if (!token.equals(bodyTokens[pos]))
            return false;

        boolean isMatchFound = false;
        boolean isBeforeToken = true;

        isMatchFound = findMatchAroundToken(bodyTokens, originalWords, tokenIndex, pos, isBeforeToken);

        if (isMatchFound) {
            isMatchFound = findMatchAroundToken(bodyTokens, originalWords, tokenIndex, pos, !isBeforeToken);
        }

        return isMatchFound;
    }

    private void processPageForPhraseMatch(PageReference page,
            List<String> originalWords,
            String originalToken,
            List<PageReference> filteredPages) {

        List<Integer> positions = page.getWordPositions();
        String bodyContent = pageReferenceService.getPageBodyContent(page);
        String[] bodyTokens = tokenizer.tokenize(bodyContent.toLowerCase());

        for (Integer pos : positions) {
            boolean isMatchFound = isPhraseMatchFound(bodyTokens, originalWords, originalToken, pos);

            if (isMatchFound) {
                System.out.println("matched:" + bodyTokens[pos] + " | at " + pos + " | id:" + page.getPageId());

                matchPositions.put(page.getPageId(), pos);
                filteredPages.add(page);
                // break if we found a match
                break;
            }
        }
    }

    private Map.Entry<String, List<PageReference>> findMinPagesToken(Map<String, List<PageReference>> queryPages) {
        return queryPages.entrySet().stream()
                .min(Comparator.comparingInt(entry -> entry.getValue().size()))
                .orElseThrow(() -> new IllegalArgumentException("No pages found"));
    }

    /**
     * Filters pages for phrase matching by going through the token with the
     * fewest pages to speed things up
     *
     * @param queryPages  A map where the key is the token, and the value is a list
     *                    of pages containing that token
     * @param queryResult query result data
     * @return The new filtered query pages map to pass to ranker
     */
    public Map<String, List<PageReference>> filterPhraseMatchPages(Map<String, List<PageReference>> queryPages,
            QueryResult queryResult) {

        // Get original words and mapping
        List<String> originalWords = queryResult.getOriginalWords();
        Map<String, String> tokenizedToOriginal = queryResult.getTokenizedToOriginal();


        Map.Entry<String, List<PageReference>> minEntry = findMinPagesToken(queryPages);
        String tokenWithMinPages = minEntry.getKey();
        List<PageReference> minTokenPages = minEntry.getValue();
        String originalWord = tokenizedToOriginal.get(tokenWithMinPages);

        // debug lines
        System.out.println("original word with min pages: " + originalWord);
        System.out.println("size of min pages: " + minTokenPages.size());

        // final filtered query pages
        List<PageReference> filteredPages = Collections.synchronizedList(new ArrayList<>());

        // Executor setup
        ExecutorService executor = Executors.newFixedThreadPool(threadsNum);
        List<Future<?>> futures = new ArrayList<>();

        for (PageReference page : minTokenPages) {
            futures.add(executor.submit(() -> {
                processPageForPhraseMatch(page, originalWords, tokenizedToOriginal.get(tokenWithMinPages),
                        filteredPages);
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // final filtered pages
        return Map.of(tokenWithMinPages, filteredPages);
    }
}
