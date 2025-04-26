package com.project.searchengine.server.service;

import com.mongodb.bulk.BulkWriteResult;
import com.project.searchengine.server.model.*;
import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import java.util.*;
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
                    existingIndices.stream().map(InvertedIndex::getWord).toList());

            // 2- Create bulk operations
            BulkOperations bulkOps = mongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED,
                    InvertedIndex.class);

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
                    update.inc("pageCount", index.getPageCount());

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
                                result.getModifiedCount());
            } catch (Exception e) {
                System.err.println("Error saving tokens: " + e.getMessage());
            }
            indexBuffer.clear();
        }
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
}
