package com.project.searchengine.server.service;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class InvertedIndexService {

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    /**
     * Gets the inverted index for a given word.
     *
     * @param word The word to get the inverted index for.
     * @return The inverted index for the given word.
     */
    public InvertedIndex getInvertedIndex(String word) {
        return invertedIndexRepository.findById(word).orElse(null);
    }
}