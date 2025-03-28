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

            // Remove punctuations, numbers, and special characters
            String cleanToken = token.replaceAll("[^a-zA-Z]", "");

            // Conevert to lower case
            cleanToken = cleanToken.toLowerCase();

            // Filter out empty tokens
            if (!cleanToken.isEmpty()) {
                tokens.computeIfAbsent(cleanToken, k -> new ArrayList<>()).add(position);
                position++;
            }

            // Move to the next word
            start = current;
            current = wordIterator.next();
        }

        return tokens;
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
        text = "Email me at user@domain.com or #hashtag!";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [email, me, at, user, domain, com, or, hashtag]

        // Test 4
        text = "Café, résumé, naïve, façade!";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [café, résumé, naïve, façade]

        // Test 5
        text = "C++ coding is FUN! Let's try 100% effort.";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [c++, coding, is, fun, let, s, try, effort]

    }
}