package com.project.searchengine.queryprocessor;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageReferenceService;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

@Component
public class SnippetGenerator {
    private static Integer halfSnippetSize = 50;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private final PorterStemmer stemmer = new PorterStemmer();

    @Autowired
    private PageReferenceService pageReferenceService;

    @Autowired
    private PhraseMatcher phraseMatcher;

    private String generateSnippet(String token,
            String[] bodyTokens,
            int matchPosition,
            QueryResult queryResult) {

        boolean isPhraseMatch = queryResult.getIsPhraseMatch();
        List<String> queryWords = queryResult.getOriginalWords();
        List<String> tokenizedQuery = queryResult.getTokenizedQuery();

        // Calculate the range of tokens to include in the snippet
        int startIndex = Math.max(0, matchPosition - halfSnippetSize);
        int endIndex = Math.min(bodyTokens.length, matchPosition + halfSnippetSize);

        StringBuilder snippet = new StringBuilder();

        String openingPunctuation = "[\\(\\[\\{-]";
        String closingPunctuation = "[.,!?;:\"'\\)\\]\\}-]";

        // Calculate the range of tokens to highlight if it's a phrase match
        Set<Integer> highlightPositions = new HashSet<>();

        if (isPhraseMatch && queryWords != null && !queryWords.isEmpty()) {
            // For phrase matching, we need to highlight multiple consecutive words
            boolean isMatchFound = phraseMatcher.isPhraseMatchFound(bodyTokens,
                    queryWords,
                    token,
                    matchPosition);

            // If a match is found, highlight the positions of the words in the phrase

            if (isMatchFound) {
                int phraseLength = queryWords.size();
                int tokenIndex = queryWords.indexOf(token);

                int start = Math.max(0, matchPosition - tokenIndex);
                int end = Math.min(bodyTokens.length - 1, start + phraseLength - 1);

                highlightPositions.add(matchPosition);

                // add them to highlight positions
                for (int i = start; i <= end; i++) {
                    highlightPositions.add(i);
                }
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            String bodyToken = bodyTokens[i];

            String stemmedToken = bodyToken.matches("[a-zA-Z]+")
                    ? stemmer.stem(bodyToken.toLowerCase())
                    : bodyToken.toLowerCase();

            String nextToken = (i + 1 < bodyTokens.length) ? bodyTokens[i + 1] : null;

            boolean isOpeningPunctuation = bodyToken.matches(openingPunctuation);
            boolean isClosingNextPunctuation = (nextToken != null) &&
                    nextToken.matches(closingPunctuation);

            // Check if current token should be highlighted
            boolean shouldHighlight = highlightPositions.contains(i);

            if (shouldHighlight || (!isPhraseMatch &&
                    tokenizedQuery.contains(stemmedToken))) {
                snippet.append("<strong>").append(bodyToken).append("</strong>");
            } else {
                snippet.append(bodyToken);
            }

            // Add spacing as needed
            if (!isOpeningPunctuation && !isClosingNextPunctuation) {
                snippet.append(" ");
            }
        }

        return snippet.toString().trim();
    }

    /**
     * Retrieve the snippet for the given page based on a search token
     *
     * @param token       A stemmed token from the search query
     * @param page        page reference to get snippet for
     * @param queryResult query result data 
     * @return A map of page ID to its snippet
     */
    public Map<String, String> getPagesSnippets(
            String token,
            PageReference page,
            QueryResult queryResult) {

        Map<String, String> pageSnippet = new HashMap<>();

        List<Integer> positions = page.getWordPositions();
        String bodyContent = pageReferenceService.getPageBodyContent(page);
        String[] bodyTokens = tokenizer.tokenize(bodyContent.toLowerCase());
        String pageId = page.getPageId();

        // get one snippet only for each page
        for (Integer pos : positions) {
            if (pos < 0 || pos >= bodyTokens.length) {
                continue;
            }

            // exclude header positions for now
            String stemmedToken = stemmer.stem(bodyTokens[pos]);
            if (!stemmedToken.equals(token)) {
                continue;
            }

            String snippet = generateSnippet(token, bodyTokens, pos, queryResult);
            pageSnippet.put(pageId, snippet);
            break;
        }

        return pageSnippet;
    }
}
