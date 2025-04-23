package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.UrlDocument;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlsFrontierRepository extends MongoRepository<UrlDocument, String> {
    /**
     * Finds the top 100 URLs sorted by frequency in descending order, returning
     * only the normalizedUrl field, for documents where isCrawled is false.
     *
     * @return List of up to 100 normalized URLs where isCrawled is false
     */
    @Query(
        value = "{ 'isCrawled': false }",
        fields = "{ 'normalizedUrl': 1, '_id': 0 }",
        sort = "{ 'frequency': -1 }"
    )
    List<String> findTop200ByFrequency();

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
     * @return false if the URL existed and was updated, true if a new document was
     *         created
     */
    default boolean upsertUrl(String normalizedUrl) {
        if (existsByNormalizedUrl(normalizedUrl)) {
            incrementFrequency(normalizedUrl);
            return true;
        } else {
            if (count() < 1000) {
                UrlDocument newDocument = new UrlDocument(
                    normalizedUrl,
                    1L,
                    false,
                    "",
                    "",
                    new ArrayList<>(),
                    ""
                );
                save(newDocument);
                return true;
            }
            return false;
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
     * @param isIndexed        The new indexed status
     * @param lastCrawled      The new last crawled date
     */
    @Query("{ 'normalizedUrl': ?0 }")
    @Update(
        "{ '$set': { 'document': ?1, 'hashedDocContent': ?2, 'linkedPages': ?3, 'isCrawled': ?4, isIndexed: ?5, 'lastCrawled': ?6 } }"
    )
    void updateUrlDocument(
        String normalizedUrl,
        String document,
        String hashedDocContent,
        List<String> linkedPages,
        boolean isCrawled,
        boolean isIndexed,
        String lastCrawled
    );

    /**
     * Finds all documents where isIndexed is false, limited to the specified number.
     *
     * @param limit The maximum number of documents to return
     * @return List of UrlDocument objects
     */
    List<UrlDocument> findByIsIndexedFalseAndIsCrawledTrue(int limit);

    /**
     * Deletes a document with the given normalizedUrl from the database.
     *
     * @param normalizedUrl The normalized URL of the document to delete
     */
    @Query(value = "{ 'normalizedUrl': ?0 }", delete = true)
    void deleteByNormalizedUrl(String normalizedUrl);

    /**
     * Retrieves all hashedDocContent values from the database.
     *
     * @return List of all hashedDocContent values
     */
    @Query(value = "{}", fields = "{ 'hashedDocContent': 1, '_id': 0 }")
    List<String> findAllHashedDocContent();
}
