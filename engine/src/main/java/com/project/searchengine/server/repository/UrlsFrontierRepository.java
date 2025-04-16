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
     * Finds the top 100 URLs sorted by frequency in descending order, returning
     * only the normalizedUrl field.
     *
     * @return List of up to 100 normalized URLs
     */
    @Query(value = "{}", fields = "{ 'normalizedUrl': 1, '_id': 0 }", sort = "{ 'frequency': -1 }")
    List<String> findTop100ByFrequency();

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
            System.out.println("Creating new document for URL: " + normalizedUrl + "\n");
            UrlDocument newDocument = new UrlDocument();
            newDocument.setNormalizedUrl(normalizedUrl);
            newDocument.setFrequency(1L);
            newDocument.setCrawled(false);
            newDocument.setDocument("");
            newDocument.setHashedDocContent("");
            newDocument.setLinkedPages(new ArrayList<>() {});
            System.out.println("New document created: " + newDocument + "\n");
            save(newDocument);
            return true;
        }
    }

    /**
     * Updates a document with the given normalizedUrl with the provided fields.
     *
     * @param normalizedUrl    The normalized URL of the document to update
     * @param document         The new HTML content of the page
     * @param hashedDocContent The new hashed content of the page
     * @param linkedPages      The new list of linked URLs
     * @param isCrawled        The new crawled status
     * @param lastCrawled      The new last crawled date
     */
    @Query("{ 'normalizedUrl': ?0 }")
    @Update("{ '$set': { 'document': ?1, 'hashedDocContent': ?2, 'linkedPages': ?3, 'isCrawled': ?4, 'lastCrawled': ?5 } }")
    void updateUrlDocument(String normalizedUrl, String document, String hashedDocContent, List<String> linkedPages,
            boolean isCrawled, String lastCrawled);
}