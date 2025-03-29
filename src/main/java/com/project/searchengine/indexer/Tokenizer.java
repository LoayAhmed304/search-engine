package com.project.searchengine.indexer;

import java.util.*;
import java.util.regex.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

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
    Map<String, List<Integer>> headerTokens = new HashMap<>();

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

    public Tokenizer() {}

    public Map<String, List<Integer>> tokenize(String text) {
        Map<String, List<Integer>> tokens = new HashMap<>();
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
                // Add the token to the map
                tokens.computeIfAbsent(cleanedToken, k -> new ArrayList<>()).add(position);

                // Update the position
                position++;
            }
        }

        return tokens;
    }

    public Map<String, Map<String, Integer>> tokenizeHeaders(Elements fieldTags) {
        Map<String, Map<String, Integer>> headerTokens = new HashMap<>(); // token => header type => count

        for (Element header : fieldTags) {
            String headerText = header.text();
            String headerType = header.tagName();
            Map<String, List<Integer>> tokens = tokenize(headerText);
            System.out.println("Header: " + headerText);
            System.out.println("Header Type: " + headerType);

            // check if the token exits in the map
            for (Map.Entry<String, List<Integer>> entry : tokens.entrySet()) {
                String token = entry.getKey();
                Integer tokenCount = entry.getValue().size();

                // check if the token exist in the map
                if (headerTokens.containsKey(token)) {
                    Map<String, Integer> headerTypeCount = headerTokens.get(token);
                    if (headerTypeCount.containsKey(headerType)) {
                        // update the count
                        Integer count = headerTypeCount.get(headerType);
                        headerTypeCount.put(headerType, count + tokenCount);
                    } else {
                        headerTypeCount.put(headerType, tokenCount);
                    }
                } else {
                    // add the token to the map
                    Map<String, Integer> headerTypeCount = new HashMap<>();
                    headerTypeCount.put(headerType, tokenCount);
                    headerTokens.put(token, headerTypeCount);
                }
            }
        }
        System.out.println("Headers: " + headerTokens);
        return headerTokens;
    }

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

    public static void main(String[] args) {
        // Example usage
        Tokenizer tokenizer = new Tokenizer();

        String[] tests = {
            "Hello, world! This is a test. 12345 test",
            "Hello, world! How's it going?",
            "Email me at user@domain.com or #hashtag!",
            "C++, coding is FUN! Let's try 100% effort.",
            "Visit https://cairo.edu or call +20123456789.",
            "A+bB",
        };

        for (String text : tests) {
            Map<String, List<Integer>> tokenPositions = tokenizer.tokenize(text);
            System.out.println("Text: " + text);
            System.out.println("Tokens: " + tokenPositions);
            System.out.println();
        }
    }
}
