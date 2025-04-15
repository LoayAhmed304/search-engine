package com.project.searchengine.indexer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for filtering out stop words from the tokenized text.
 * It loads a list of stop words from a file and provides a method to check if a token is a stop word.
 */
@Component
public class StopWordFilter {

    private final Set<String> stopWords;

    public StopWordFilter() {
        // Load stop words from a file
        try {
            this.stopWords = new HashSet<>(
                Files.readAllLines(Paths.get("src/main/resources/stopwords.txt"))
            );
        } catch (IOException e) {
            System.err.println("Error loading stop words: " + e.getMessage());
            throw new RuntimeException("Failed to load stop words", e);
        }
    }

    /**
     * Check if a token is a stop word.
     * @param token
     * @return true if the token is a stop word, false otherwise
     */
    public boolean isStopWord(String token) {
        return stopWords.contains(token);
    }
}
