package com.project.searchengine.indexer;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.PageReference;
import java.util.*;
import java.util.regex.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class Tokenizer {

    // Define individual patterns (lowercase only)
    private final String WORD_PATTERN = "\\w+";
    private final String EMAIL_PATTERN = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}";
    private final String PHONE_PATTERN = "\\+?\\d{1,3}[-\\d]{6,12}";
    private final String HYPHENATED_PATTERN = "[a-z]+-[a-z]+";
    private final String HASHTAG_PATTERN = "#[a-z0-9_]+";
    private final String PLUS_COMBINED_PATTERN = "[a-z]+\\+{1,2}[a-z0-9]*"; // C++ like terms

    // Field-specefic tokens
    private Map<String, InvertedIndex> indexBuffer = new HashMap<>();
    private Integer tokenCount = 0;

    // Combine patterns with proper grouping
    private final Pattern pattern = Pattern.compile(
        "(" +
        EMAIL_PATTERN +
        ")|" +
        "(" +
        PHONE_PATTERN +
        ")|" +
        "(" +
        HASHTAG_PATTERN +
        ")|" +
        "(" +
        PLUS_COMBINED_PATTERN +
        ")|" +
        "(" +
        HYPHENATED_PATTERN +
        ")|" +
        "(" +
        WORD_PATTERN +
        ")"
    );

    /**
     * Tokenizes the input text and build the inverted index
     * @param text The input text to tokenize
     * @param pageId The current page id
     * @param fieldType The field type (e.g., body, title, h1, h2)
     * @param pageRank The rank of the page computed by the ranker
     */
    public void tokenizeContent(String text, String pageId, String fieldType, Double pageRank) {
        int position = 0;

        // Convert to lower case
        text = text.toLowerCase();

        // Match the pattern
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String token = matcher.group();
            String cleanedToken = cleanToken(token);

            if (!cleanedToken.isEmpty()) {
                buildInvertedIndex(cleanedToken, pageId, position, fieldType, pageRank);

                tokenCount++;
                position++;
            }
        }
    }

    /**
     * Tokenizes the headers
     * @param fieldTags The field tags to tokenize.
     */

    public void tokenizeHeaders(Elements fieldTags, String pageId, Double pageRank) {
        for (Element header : fieldTags) {
            String headerText = header.text();
            if (headerText == null || headerText.isBlank()) continue;
            String headerType = header.tagName();
            tokenizeContent(headerText, pageId, headerType, pageRank);
        }
    }

    /**
     * Build the inverted index to be saved to the database
     * If it already exists update it
     * @param word The Id of the inverted index
     * @param pageId The Id of the page that contains the word
     * @param position The position of the word in the page
     * @param fieldType  The field type (e.g., body, title, h1, h2)
     * @param pageRank  The rank of the page computed by the ranker
     */
    private void buildInvertedIndex(
        String word,
        String pageId,
        Integer position,
        String fieldType,
        Double pageRank
    ) {
        // Get or create inverted index from buffer
        InvertedIndex invertedIndex = indexBuffer.computeIfAbsent(word, w -> new InvertedIndex(word)
        );

        // Update or create pageReference
        PageReference pageReference = invertedIndex
            .getPages()
            .stream()
            .filter(p -> p.getPageId().equals(pageId))
            .findFirst()
            .orElseGet(() -> {
                PageReference newPageReference = new PageReference(pageId, 0, pageRank);
                invertedIndex.addPage(newPageReference);
                return newPageReference;
            });

        // Update positions
        pageReference.getWordPositions().add(position);

        // Update fieldsCount
        pageReference.getFieldWordCount().merge(fieldType, 1, Integer::sum);

        // Set number of pages that contains the token
        invertedIndex.setPageCount(invertedIndex.getPages().size());
    }

    /**
     * Cleans the token by removing unwanted characters.
     * Preserves special tokens like email, phone, hashtags, and hyphenated words.
     * @param token The token to clean.
     * @return The cleaned token.
     */
    private String cleanToken(String token) {
        // Preserve special tokens
        if (
            token.matches(EMAIL_PATTERN) ||
            token.matches(PHONE_PATTERN) ||
            token.matches(HYPHENATED_PATTERN) ||
            token.matches(HASHTAG_PATTERN) ||
            token.matches(PLUS_COMBINED_PATTERN)
        ) {
            return token;
        }

        // Default: remove everything except letters
        return token.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * Return index buffer of all tokens of the current document
     * @return Buffer of tokens
     */
    public Map<String, InvertedIndex> getIndexBuffer() {
        return indexBuffer;
    }
}
