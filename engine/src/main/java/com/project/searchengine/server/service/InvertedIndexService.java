package com.project.searchengine.server.service;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import com.project.searchengine.server.model.*;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import com.project.searchengine.server.service.PageService;
import java.util.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

@Service
public class InvertedIndexService {

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PageService pageService;

    /**
     * Gets the inverted index for a given word.
     *
     * @param word The word to get the inverted index for.
     * @return The inverted index for the given word.
     */
    public InvertedIndex getInvertedIndex(String word) {
        InvertedIndex result = invertedIndexRepository.findByWord(word);
        return result;
    }

    /**
     * Saves a list of inverted indices in bulk to the database.
     *
     * @param indexBuffer Map of word to InvertedIndex objects to be saved
     */
    public void saveTokensInBulk(Map<String, InvertedIndex> indexBuffer) {
        if (!indexBuffer.isEmpty()) {
            // 1- Query existing words from the DB
            Set<String> allWords = indexBuffer.keySet();
            List<InvertedIndex> existingIndices = invertedIndexRepository.findAllByWordIn(allWords);
            Set<String> existingWordSet = new HashSet<>(
                existingIndices.stream().map(InvertedIndex::getWord).toList()
            );

            // 2- Create bulk operations
            BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                InvertedIndex.class
            );

            // 3- Insert or update the indices
            for (InvertedIndex index : indexBuffer.values()) {
                String word = index.getWord();
                Query query = new Query(Criteria.where("word").is(word));
                Update update = new Update();
                if (existingWordSet.contains(word)) {
                    // Update existing word
                    for (PageReference page : index.getPages()) {
                        // Update the page reference
                        update.addToSet("pages", page);
                    }

                    bulkOps.updateOne(query, update);
                } else {
                    // Insert new word
                    bulkOps.insert(index);
                }
            }

            try {
                BulkWriteResult result = bulkOps.execute();
                System.out.println(
                    "Inserted: " +
                    result.getInsertedCount() +
                    ", Updated: " +
                    result.getModifiedCount()
                );
            } catch (Exception e) {
                System.err.println("Error saving tokens: " + e.getMessage());
            }
            indexBuffer.clear();
        }
    }

    /**
     * Retrieves the pages associated with a given token.
     *
     * @param token The token to search for.
     * @return A list of PageReference objects associated with the token.
     */
    public List<PageReference> getTokenPages(String token) {
        InvertedIndex invertedIndex = invertedIndexRepository.findByWord(token);
        if (invertedIndex != null) {
            return invertedIndex.getPages();
        } else {
            return Collections.emptyList();
        }
    }

    public void updateIdf() {
        try {
            // Get the total number of pages from PageService
            long totalPages = pageService.getTotalDocuments();
            if (totalPages == 0) {
                System.err.println("No pages found in the pages collection. Skipping IDF update.");
                return;
            }

            // Retrieve all InvertedIndex documents
            List<InvertedIndex> indices = invertedIndexRepository.findAll();
            if (indices.isEmpty()) {
                System.out.println("No inverted indices found. Skipping IDF update.");
                return;
            }

            // Initialize bulk operations
            BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                InvertedIndex.class
            );

            // Calculate and update IDF for each InvertedIndex
            int updateCount = 0;
            for (InvertedIndex index : indices) {
                int pageCount = index.getPages() != null ? index.getPages().size() : 0;
                double idf;
                if (pageCount == 0) {
                    idf = 0.0; // Handle zero document frequency
                } else {
                    idf = Math.log10((double) totalPages / pageCount); // log10(totalPages / size of pages)
                }

                // Create update query for the specific word
                Query query = new Query(Criteria.where("word").is(index.getWord()));
                Update update = new Update().set("idf", idf);

                bulkOps.updateOne(query, update);
                updateCount++;
            }

            // Execute bulk updates
            if (updateCount > 0) {
                BulkWriteResult result = bulkOps.execute();
                System.out.println("Updated IDF for " + result.getModifiedCount() + " documents");
            } else {
                System.out.println("No documents to update for IDF.");
            }
        } catch (Exception e) {
            System.err.println("Error updating IDF: " + e.getMessage());
        }
    }
}
