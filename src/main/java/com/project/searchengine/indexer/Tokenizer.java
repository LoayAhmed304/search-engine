package com.project.searchengine.indexer;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import java.util.*;
import java.util.regex.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.data.mongodb.core.query.Criteria;
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
    Map<String, List<Integer>> bodyTokens = new HashMap<>();
    Map<String, List<Integer>> titleTokens = new HashMap<>();
    Map<String, Map<String, Integer>> headerTokens = new HashMap<>();
    private Map<String, InvertedIndex> indexBuffer = new HashMap<>();
    private Integer tokenCount = 0;

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

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
     * Tokenizes the input text and returns a map of tokens with their positions.
     * @param text The input text to tokenize.
     * @return A map where the key is the token and the value is a list of positions.
     */
    public void tokenizeContent(String text, String pageId, String fieldType, Double pageRank) {
        int position = 0;

        // Convert to lower case
        text = text.toLowerCase();

        // Match the pattern
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // Get the matched token
            String token = matcher.group();
            String cleanedToken = cleanToken(token);

            if (!cleanedToken.isEmpty()) {
                // Save the token to the inverted index
                saveToken(cleanedToken, pageId, position, fieldType, pageRank);

                this.tokenCount++;
                // Update the position
                position++;
            }
        }
    }

    /**
     * Tokenizes the headers and returns a map of tokens with their header types and counts.
     * @param fieldTags The field tags to tokenize.
     * @return A map where the key is the token and the value is another map with header types and their counts.
     */

    public void tokenizeHeaders(Elements fieldTags, String pageId, Double pageRank) {
        for (Element header : fieldTags) {
            String headerText = header.text();
            if (headerText == null || headerText.isBlank()) continue;
            String headerType = header.tagName();
            tokenizeContent(headerText, pageId, headerType, pageRank);
        }
    }

    private void saveToken(
        String word,
        String pageId,
        Integer position,
        String fieldType,
        Double pageRank
    ) {
        // Get or create inverted index from buffer
        InvertedIndex invertedIndex = indexBuffer.computeIfAbsent(word, w -> new InvertedIndex(word)
        );

        // 3- Update pageReference
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

        // 4- Update positions
        pageReference.getWordPositions().add(position);

        // 5- Update fieldsCount
        pageReference.getFieldWordCount().merge(fieldType, 1, Integer::sum);

        // 6- Set number of pages that contains the token
        invertedIndex.setPageCount(invertedIndex.getPages().size());
    }

    public void saveTokens() {
        System.out.println("Tokens: " + indexBuffer.keySet() + " size: " + indexBuffer.size());
        if (!indexBuffer.isEmpty()) {
            long start = System.nanoTime();
            BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                InvertedIndex.class
            );

            for (InvertedIndex index : indexBuffer.values()) {
                Query query = new Query(Criteria.where("word").is(index.getWord()));
                Update update = new Update()
                    .set("pages", index.getPages())
                    .set("pageCount", index.getPageCount());
                bulkOps.upsert(query, update);
            }
            bulkOps.execute();
            long duration = (System.nanoTime() - start) / 1_000_000;
            System.out.println("saveTokens took: " + duration + " ms");
            indexBuffer.clear();
        }
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
}
