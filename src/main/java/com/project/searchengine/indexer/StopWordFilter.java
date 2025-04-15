package com.project.searchengine.indexer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class StopWordFilter {

    private final Set<String> stopWords;

    public StopWordFilter() {
        try {
            this.stopWords = new HashSet<>(
                Files.readAllLines(Paths.get("src/main/resources/stopwords.txt"))
            );
        } catch (IOException e) {
            System.err.println("Error loading stop words: " + e.getMessage());
            throw new RuntimeException("Failed to load stop words", e);
        }

        System.out.println("Stop words loaded: " + this.stopWords + "\n");
        System.out.println("Stop words size: " + this.stopWords.size() + "\n");
    }

    public boolean isStopWord(String token) {
        return stopWords.contains(token);
    }
}
