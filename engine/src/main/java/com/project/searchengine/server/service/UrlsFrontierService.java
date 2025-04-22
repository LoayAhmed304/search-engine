package com.project.searchengine.server.service;

import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.repository.UrlsFrontierRepository;
import com.project.searchengine.utils.JsonParserUtil;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UrlsFrontierService {

    private UrlsFrontierRepository urlsFrontierRepository;

    @Autowired
    public UrlsFrontierService(UrlsFrontierRepository urlsFrontierRepository) {
        this.urlsFrontierRepository = urlsFrontierRepository;
    }

    /**
     * Retrieves the top 100 URLs sorted by frequency in descending order.
     *
     * @return List of up to 100 UrlDocument objects
     */
    public List<String> getTop100UrlsByFrequency() {
        List<String> topUrls = urlsFrontierRepository.findTop100ByFrequency();

        return JsonParserUtil.parseSingleField(topUrls, "normalizedUrl");
    }

    /**
     * Increments the frequency of a URL identified by its normalized URL.
     *
     * @param normalizedUrl The normalized URL to update
     */
    public void incrementFrequency(String normalizedUrl) {
        urlsFrontierRepository.incrementFrequency(normalizedUrl);
    }

    /**
     * Checks if a normalized URL exists in the frontier.
     *
     * @param normalizedUrl The normalized URL to check
     * @return true if the URL exists, false otherwise
     */
    public boolean existsByNormalizedUrl(String normalizedUrl) {
        return urlsFrontierRepository.existsByNormalizedUrl(normalizedUrl);
    }

    /**
     * Upserts a URL: increments frequency by 1 if it exists; otherwise, creates a new document
     * with frequency = 1.
     *
     * @param url The original URL to upsert
     */
    public boolean upsertUrl(String url) {
        return urlsFrontierRepository.upsertUrl(url);
    }

    /**
     * Initializes the frontier with a list of seed URLs.
     *
     * @param seedUrls List of seed URLs to insert
     */
    public void initializeFrontier(List<String> seedUrls) {
        for (String url : seedUrls) {
            upsertUrl(url);
        }
    }

    /**
     *
     * @return list of all Urls stored (not necessarily crawled)
     */
    public List<UrlDocument> getAllUrls() {
        return urlsFrontierRepository.findAll();
    }

    /**
     * Checks if the frontier is empty.
     * @return
     */
    public boolean isEmpty() {
        return urlsFrontierRepository.count() == 0;
    }

    /**
     * Updates a document with the given normalizedUrl with the provided fields.
     *
     * @param normalizedUrl    The normalized URL of the document to update
     * @param document         The new HTML content of the page
     * @param hashedDocContent The new hashed content of the page
     * @param linkedPages      The new list of linked URLs
     * @param isCrawled        The new crawled status
     */
    public void updateUrlDocument(UrlDocument doc) {
        urlsFrontierRepository.updateUrlDocument(
            doc.getNormalizedUrl(),
            doc.getDocument(),
            doc.getHashedDocContent(),
            doc.getLinkedPages(),
            doc.isCrawled(),
            doc.getLastCrawled()
        );
    }

    /**
     * Retrieves the count of documents in the frontier.
     *
     * @return The count of documents
     */
    public int count() {
        return (int) urlsFrontierRepository.count();
    }

    /**
     * Saves all given URL documents into the database
     * @param urls  All URLs to be saved into the database
     * @return All URLs saved into the database successfully
     */
    public List<UrlDocument> saveAll(List<UrlDocument> urls) {
        return urlsFrontierRepository.saveAll(urls);
    }

    /**
     * Deletes a document with the given normalizedUrl from the database.
     *
     * @param normalizedUrl The normalized URL of the document to delete
     */
    public void deleteByNormalizedUrl(String normalizedUrl) {
        urlsFrontierRepository.deleteByNormalizedUrl(normalizedUrl);
    }

    /**
     * Retrieves all hashedDocContent values from the database.
     *
     * @return List of all hashedDocContent values
     */
    public List<String> findAllHashedDocContent() {
       List<String> allHash = urlsFrontierRepository.findAllHashedDocContent();
       return JsonParserUtil.parseSingleField(allHash, "hashedDocContent");
    }
}
