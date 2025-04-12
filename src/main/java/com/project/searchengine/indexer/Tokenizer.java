package com.project.searchengine.indexer;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import java.util.*;
import java.util.regex.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

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
    public Map<String, List<Integer>> tokenizeContent(
        String text,
        String pageId,
        String fieldType,
        Double pageRank
    ) {
        Map<String, List<Integer>> tokens = new HashMap<>();
        int position = 0;
        int tokenCount = 0;

        // Convert to lower case
        text = text.toLowerCase();

        // Match the pattern
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // Get the matched token
            String token = matcher.group();
            String cleanedToken = cleanToken(token);

            if (!cleanedToken.isEmpty()) {
                // Add the token to the map
                tokens.computeIfAbsent(cleanedToken, k -> new ArrayList<>()).add(position);

                // Save the token to the inverted index
                saveToken(
                    cleanedToken,
                    pageId,
                    position,
                    Map.of(fieldType, 1),
                    tokenCount,
                    pageRank
                );
                tokenCount++;

                // Update the position
                position++;
            }
        }

        return tokens;
    }

    /**
     * Tokenizes the headers and returns a map of tokens with their header types and counts.
     * @param fieldTags The field tags to tokenize.
     * @return A map where the key is the token and the value is another map with header types and their counts.
     */
    public Map<String, Map<String, Integer>> tokenizeHeaders(
        Elements fieldTags,
        String pageId,
        Double pageRank
    ) {
        headerTokens = new HashMap<>(); // token => header type => count

        for (Element header : fieldTags) {
            String headerText = header.text();
            String headerType = header.tagName();
            Map<String, List<Integer>> tokens = tokenizeContent(
                headerText,
                pageId,
                headerType,
                pageRank
            );

            for (Map.Entry<String, List<Integer>> entry : tokens.entrySet()) {
                String token = entry.getKey();
                Integer tokenCount = entry.getValue().size();

                // Check if the token exists in the map
                if (headerTokens.containsKey(token)) {
                    Map<String, Integer> headerTypeCount = headerTokens.get(token);
                    if (headerTypeCount.containsKey(headerType)) {
                        // Update the count
                        Integer count = headerTypeCount.get(headerType);
                        headerTypeCount.put(headerType, count + tokenCount);
                    } else {
                        headerTypeCount.put(headerType, tokenCount);
                    }
                } else {
                    // Add the token to the map
                    Map<String, Integer> headerTypeCount = new HashMap<>();
                    headerTypeCount.put(headerType, tokenCount);
                    headerTokens.put(token, headerTypeCount);
                }
            }
        }
        return headerTokens;
    }

    private void saveToken(
        String word,
        String pageId,
        Integer position,
        Map<String, Integer> fieldsCount,
        Integer pageTokens,
        Double pageRank
    ) {
        // 1- Check if the token already exists in the database
        Optional<InvertedIndex> optIndex = invertedIndexRepository.findById(word);

        // 2- If it does not exist, create a new InvertedIndex object
        InvertedIndex invertedIndex = optIndex.orElseGet(() -> new InvertedIndex(word));

        // 3- Update pageReference
        PageReference pageReference = invertedIndex
            .getPages()
            .stream()
            .filter(p -> p.getPageId().equals(pageId))
            .findFirst()
            .orElseGet(() -> {
                PageReference newPageReference = new PageReference(pageId, pageTokens, pageRank);
                invertedIndex.getPages().add(newPageReference);
                return newPageReference;
            });

        // 4- Update positions
        pageReference.getWordPositions().add(position);

        // 5- Update fieldsCount
        if (fieldsCount != null) {
            for (Map.Entry<String, Integer> entry : fieldsCount.entrySet()) {
                String field = entry.getKey();
                Integer count = entry.getValue();
                pageReference.getFieldWordCount().merge(field, count, Integer::sum);
            }
        }

        // 6- Set number of pages that contains the token
        invertedIndex.setPageCount(invertedIndex.getPages().size());

        // 7- Save the inverted index to the database
        invertedIndexRepository.save(invertedIndex);
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
    // Main method for testing
    // public static void main(String[] args) {
    //     // Example usage
    //     Tokenizer tokenizer = new Tokenizer();

    //     String[] tests = {
    //         "Hello, world! This is a test. 12345 test",
    //         "Hello, world! How's it going?",
    //         "Email me at user@domain.com or #hashtag!",
    //         "C++, coding is FUN! Let's try 100% effort.",
    //         "Visit https://cairo.edu or call +20123456789.",
    //         "A+bB",
    //     };

    //     for (String text : tests) {
    //         Map<String, List<Integer>> tokenPositions = tokenizer.tokenizeContent(
    //             text,
    //             "pageId",
    //             "body",
    //             0.0
    //         );
    //         System.out.println("Text: " + text);
    //         System.out.println("Tokens: " + tokenPositions);
    //         System.out.println();
    //     }
    // }
}
