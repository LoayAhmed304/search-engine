package com.project.searchengine.queryprocessor;

import java.io.InputStream;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageReferenceService;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

@Component
public class SnippetGenerator {
    private ThreadLocal<TokenizerME> tokenizerLocal;
    private final PorterStemmer stemmer = new PorterStemmer();

    private static Integer halfSnippetSize = 50;

    @Autowired
    private PageReferenceService pageReferenceService;

    @Autowired
    private PhraseMatcher phraseMatcher;

    SnippetGenerator() {
        // Load the tokenizer model
        loadTokenizerModel();
    }

    void loadTokenizerModel() {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/en-token.bin")) {
            if (modelIn == null) {
                throw new IllegalArgumentException("Model file not found");
            }
            TokenizerModel model = new TokenizerModel(modelIn);
            tokenizerLocal = ThreadLocal.withInitial(() -> new TokenizerME(model));
        } catch (Exception e) {
            throw new RuntimeException("Error loading tokenizer model", e);
        }
    }

    /**
     * Generates a snippet from the body of text by extracting a range of tokens
     * and optionally highlights query words and phrases
     * 
     * @param token         The token from the query that was matched in the body
     *                      text
     * @param bodyTokens    The array of body tokens from the page
     * @param matchPosition The match position of the token in the body
     * @param queryResult   The query result data
     * @return A formatted snippet string from the body of text, highlighting the
     *         matched tokens
     */
    private String generateSnippet(String token,
            String[] bodyTokens,
            int matchPosition,
            QueryResult queryResult) {

        boolean isPhraseMatch = queryResult.getIsPhraseMatch();
        List<String> queryWords = queryResult.getOriginalWords();
        List<String> tokenizedQuery = queryResult.getTokenizedQuery();
        Map<String, String> tokenizedToOriginal = queryResult.getTokenizedToOriginal();

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
                    tokenizedToOriginal.get(token),
                    matchPosition);

            // If a match is found, highlight the positions of the words in the phrase
            if (isMatchFound) {
                // Get the length of the phrase
                int phraseLength = queryWords.size();
                // Get index of original word in query before tokenization
                int tokenIndex = queryWords.indexOf(tokenizedToOriginal.get(token));

                // debug lines not removing now
                System.out.println("original query size: " + phraseLength);
                System.out.println("token provided: " + tokenizedToOriginal.get(token));
                System.out.println("token index in original query: " + tokenIndex);
                System.out.println("match pos in body: " + matchPosition);

                int start = Math.max(0, matchPosition - tokenIndex);
                int end = Math.min(bodyTokens.length, start + phraseLength);

                highlightPositions.add(matchPosition);

                // Add phrase words to highlight positions
                for (int i = start; i < end; i++) {
                    System.out.print(i + " ");
                    highlightPositions.add(i);
                }
                System.out.println();
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            String bodyToken = bodyTokens[i];

            // get stemmed token for highlighting any other query tokens
            String stemmedToken;
            try {
                stemmedToken = (bodyToken != null && !bodyToken.isEmpty() && bodyToken.matches("[a-zA-Z]+"))
                        ? stemmer.stem(bodyToken.toLowerCase())
                        : bodyToken.toLowerCase();
            } catch (Exception e) {
                stemmedToken = bodyToken.toLowerCase();
            }

            String nextToken = (i + 1 < bodyTokens.length) ? bodyTokens[i + 1] : null;

            boolean isOpeningPunctuation = bodyToken.matches(openingPunctuation);
            boolean isClosingNextPunctuation = (nextToken != null) &&
                    nextToken.matches(closingPunctuation);

            // Check if current token should be highlighted
            boolean shouldHighlight = highlightPositions.contains(i)
                    || (!isPhraseMatch && tokenizedQuery.contains(stemmedToken));

            if (shouldHighlight) {
                snippet.append("<strong>").append(bodyToken).append("</strong>");
            } else {
                snippet.append(bodyToken);
            }

            // Add spacing as needed for punctuation
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
     * @param page        The page reference to get snippet for
     * @param queryResult The query result data
     * @return A map of page ID to its snippet
     */
    public Map<String, String> getPagesSnippets(
            String token,
            PageReference page,
            QueryResult queryResult) {

        Map<String, String> pageSnippet = new HashMap<>();

        List<Integer> positions = page.getWordPositions();
        String bodyContent = pageReferenceService.getPageBodyContent(page);

        String[] bodyTokens = tokenizerLocal.get().tokenize(bodyContent.toLowerCase());

        String pageId = page.getPageId();
        boolean isPhraseMatch = queryResult.getIsPhraseMatch();

        // Get one snippet only for each page
        for (Integer pos : positions) {
            // Get the match position directly for the token for phrase match queries
            if (isPhraseMatch)
                pos = phraseMatcher.getMatchPosition(pageId);

            String snippet = generateSnippet(token, bodyTokens, pos, queryResult);
            pageSnippet.put(pageId, snippet);
            break;
        }

        // Clean up the tokenizer
        tokenizerLocal.remove();
        return pageSnippet;
    }
}
