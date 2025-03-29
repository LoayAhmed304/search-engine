package com.project.searchengine.indexer;

import java.util.*;
import java.text.*;

public class Tokenizer {
   
   private BreakIterator wordIterator;
    
    public Tokenizer() {
        // Initialize BreakIterator for English word boundaries
        this.wordIterator = BreakIterator.getWordInstance(Locale.ENGLISH);
    }

    public Map<String, List<Integer>> tokenize(String text) {
        Map<String, List<Integer>> tokens = new HashMap<>();

        // Set the text for the BreakIterator
        wordIterator.setText(text);

        // Find the start and end of each word
        int start = wordIterator.first();
        int current = wordIterator.next();
        int position = 0;

        // Iterate through the text and extract words
        while(current != BreakIterator.DONE) {
            String token = text.substring(start, current).trim();

            // Convert to lower case
            String lowerCaseToken = token.toLowerCase();

            // Remove punctuations, numbers, and special characters
            String cleanToken = cleanToken(lowerCaseToken);

            // Filter out empty tokens
            if (!cleanToken.isEmpty()) {
                // Stemming

                
                tokens.computeIfAbsent(cleanToken, k -> new ArrayList<>()).add(position);
                position++;
            }

            // Move to the next word
            start = current;
            current = wordIterator.next();
        }

        return tokens;
    }

    private String cleanToken(String token) {
        // Handle Hyphenated links
        if (token.matches("(?=\\S*['-])([a-zA-Z'-]+)")) {
            return token;
        }

        // Default: remove everything except letters
        return token.replaceAll("[^a-z]", "");
    }

    public static void main(String[] args) {
        // Example usage
        Tokenizer tokenizer = new Tokenizer();

        // Test 1
        String text = "Hello, world! This is a test. 12345";
        Map<String, List<Integer>> tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [hello, world, this, is, a, test]
        

        // Test 2
        text = "Hello, world! How's it going?";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [hello, world, how, s, it, going]

        // Test 3
        text = "Email me at user@domain.com or #hashtag! +18143512533 https://stackoverflow.com/questions/31910955/regex-to-match-words-with-hyphens-and-or-apostrophes";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [email, me, at, user, domain, com, or, hashtag]

        // Test 4
        text = "C++ coding is FUN! Let's try 100% effort. hyphenated-word";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [c++, coding, is, fun, let, s, try, effort]


        // Test 4
        text = "Café, résumé";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [c++, coding, is, fun, let, s, try, effort]

    }
}