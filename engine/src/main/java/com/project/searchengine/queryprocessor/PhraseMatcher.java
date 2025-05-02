package com.project.searchengine.queryprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        int offset = isBeforeToken ? pos - 1 : pos + 1;
        int currentTokenIndex = isBeforeToken ? tokenIndex - 1 : tokenIndex + 1;
        int querySize = originalWords.size();

        while (found &&
                isValidOffset(offset, bodyTokens.length)
                && isValidTokenIndex(currentTokenIndex, querySize)) {

            String currentToken = originalWords.get(currentTokenIndex);

            if (!currentToken.equals(bodyTokens[offset])) {
                found = false;
            }

            offset = isBeforeToken ? offset - 1 : offset + 1;
            currentTokenIndex = isBeforeToken ? currentTokenIndex - 1 : currentTokenIndex + 1;
            currentTokenIndex++;
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

        boolean isMatchFound = false;
        boolean isBeforeToken = true;

        isMatchFound = findMatchAroundToken(bodyTokens, originalWords, tokenIndex, pos, isBeforeToken);

        if (isMatchFound) {
            isMatchFound = findMatchAroundToken(bodyTokens, originalWords, tokenIndex, pos, !isBeforeToken);
        }

        return isMatchFound;
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
     * Filters pages for phrase matching by going through the  token with the
     * fewest pages to speed things up
     *
     * @param queryPages  A map where the key is the token, and the value is a list
     *                    of pages containing that token
     * @param queryResult query result data
     * @return The new filtered query pages map to pass to ranker
     */
    public Map<String, List<PageReference>> filterPhraseMatchPages(Map<String, List<PageReference>> queryPages,
            QueryResult queryResult) {
        // get token which has min number of pages first
        String minPagesToken = getMinPagesToken(queryPages);
        List<PageReference> minPages = queryPages.get(minPagesToken);

        // filter phrase searching based on it
        Map<String, List<PageReference>> filteredQueryPages = new HashMap<>();
        List<PageReference> filteredPagesTokn = new ArrayList<>();
        List<String> originalWords = queryResult.getOriginalWords();

        for (PageReference page : minPages) {
            List<Integer> positions = page.getWordPositions();

            String bodyContent = pageReferenceService.getPageBodyContent(page);
            String[] bodyTokens = tokenizer.tokenize(bodyContent.toLowerCase());

            for (Integer pos : positions) {
                boolean isMatchFound = isPhraseMatchFound(bodyTokens, originalWords, minPagesToken, pos);

                // if one match is found, add the page to the filtered list
                if (isMatchFound) {
                    filteredPagesTokn.add(page);
                    // pageTokensCache.put(page.getPageId(), bodyTokens);
                    break;
                }
            }
        }

        filteredQueryPages.put(minPagesToken, filteredPagesTokn);
        return filteredQueryPages;
    }
}
