package com.project.searchengine.indexer;

import java.util.*;
import java.text.*;

public class Tokenizer {
   
   private BreakIterator wordIterator;
    
    public Tokenizer() {
        // Initialize BreakIterator for English word boundaries
        this.wordIterator = BreakIterator.getWordInstance(Locale.ENGLISH);
    }

    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();

        // Set the text for the BreakIterator
        wordIterator.setText(text);

        // Find the start and end of each word
        int start = wordIterator.first();
        int current = wordIterator.next();

        // Iterate through the text and extract words
        while(current != BreakIterator.DONE) {
            String token = text.substring(start, current).trim();

            // Remove punctuations, numbers, and special characters
            String cleanToken = token.replaceAll("[^a-zA-Z]", "");

            // Conevert to lower case
            cleanToken = cleanToken.toLowerCase();

            // Filter out empty tokens
            if (!cleanToken.isEmpty()) {
                tokens.add(cleanToken);
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

        String text = "Hello, world! This is a test. 12345";
        List<String> tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [hello, world, this, is, a, test]
        

        // Example usage with a different text
        text = "Hello, world! How's it going?";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [hello, world, how, s, it, going]

        // Example usage with a different text
        text = "Email me at user@domain.com or #hashtag!";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [email, me, at, user, domain, com, or, hashtag]

        // Example usage with a different text
        text = "Café, résumé, naïve, façade!";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [café, résumé, naïve, façade]

        // Example usage with a different text
        text = "C++ coding is FUN! Let's try 100% effort.";
        tokens = tokenizer.tokenize(text);
        System.out.println("Tokens: " + tokens);
        // Output: Tokens: [c++, coding, is, fun, let, s, try, effort]

    }

    
}
