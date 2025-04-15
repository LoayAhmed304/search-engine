package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.UrlDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface UrlsFrontierRepository extends MongoRepository<UrlDocument, String> {

    /**
     * Retrieves the top 100 documents sorted by frequency in descending order.
     *
     * @return List of up to 100 UrlDocument documents
     */
    @Query(value = "{}", sort = "{ 'frequency': -1 }")
    List<UrlDocument> findTop100ByFrequency();

    /**
     * Increments the frequency of a document with the given normalizedUrl.
     *
     * @param normalizedUrl The normalized URL to update
     */
    @Query("{ 'normalizedUrl': ?0 }")
    @Update("{ '$inc': { 'frequency': 1 } }")
    void incrementFrequency(String normalizedUrl);

    /**
     * Checks if a document with the given normalizedUrl exists.
     *
     * @param normalizedUrl The normalized URL to check
     * @return true if the URL exists, false otherwise
     */
    boolean existsByNormalizedUrl(String normalizedUrl);

    /**
     * Inserts a new document with the given normalizedUrl and default values.
     * Note: This is handled by save() in MongoRepository, provided for clarity.
     *
     * @param document The UrlDocument to insert
     * @return The saved UrlDocument
     */
    UrlDocument save(UrlDocument document);

    /**
     * Increments frequency by 1 if the normalizedUrl exists; otherwise, creates a
     * new document
     * with frequency = 1 and default values.
     *
     * @param normalizedUrl The normalized URL to upsert
     * @return false if the URL existed and was updated, true if a new document was created
     */
    default boolean upsertUrl(String normalizedUrl) {
        if (existsByNormalizedUrl(normalizedUrl)) {
            incrementFrequency(normalizedUrl);
            return false;
        } else {
            UrlDocument newDocument = new UrlDocument();
            newDocument.setNormalizedUrl(normalizedUrl);
            newDocument.setFrequency(1L);
            newDocument.setCrawled(false);
            newDocument.setLinkedPages(new ArrayList<>() {});
            save(newDocument);
            return true;
        }
    }
}