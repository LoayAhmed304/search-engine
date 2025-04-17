package com.project.searchengine.server.service;

import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.repository.UrlsFrontierRepository;
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
    public List<UrlDocument> getTop100UrlsByFrequency() {
        List<UrlDocument> topUrls = urlsFrontierRepository.findTop100ByFrequency();
        return topUrls;
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
}
