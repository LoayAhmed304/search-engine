package com.project.searchengine.server.service;

import com.mongodb.bulk.BulkWriteResult;
import com.project.searchengine.server.model.*;
import com.project.searchengine.server.repository.UrlsFrontierRepository;
import com.project.searchengine.utils.JsonParserUtil;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

@Service
public class UrlsFrontierService {

    private final UrlsFrontierRepository urlsFrontierRepository;
    private final MongoOperations mongoOperations;

    @Autowired
    public UrlsFrontierService(UrlsFrontierRepository urlsFrontierRepository, MongoOperations mongoOperations) {
        this.urlsFrontierRepository = urlsFrontierRepository;
        this.mongoOperations = mongoOperations;
    }

    /**
     * Retrieves the top 100 URLs sorted by frequency in descending order.
     *
     * @return List of up to 100 normalized URLs
     */
    public List<String> getTop100UrlsByFrequency() {
        List<UrlDocument> topDocs = urlsFrontierRepository.findTop100ByIsCrawledFalseOrderByFrequencyDesc();
        return topDocs.stream()
            .map(UrlDocument::getNormalizedUrl)
            .collect(Collectors.toList());
    }

    public void incrementFrequency(String normalizedUrl) {
        urlsFrontierRepository.incrementFrequency(normalizedUrl);
    }

    public boolean existsByNormalizedUrl(String normalizedUrl) {
        return urlsFrontierRepository.existsByNormalizedUrl(normalizedUrl);
    }

    public boolean upsertUrl(String url) {
        return urlsFrontierRepository.upsertUrl(url);
    }

    public void initializeFrontier(List<String> seedUrls) {
        for (String url : seedUrls) {
            upsertUrl(url);
        }
    }

    public List<UrlDocument> getAllUrls() {
        return urlsFrontierRepository.findAll();
    }

    public boolean isEmpty() {
        return urlsFrontierRepository.count() == 0;
    }

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

    public void bulkSaveCrawledBatch(Map<String, UrlDocument> crawledDocs) {
        if (crawledDocs.isEmpty()) {
            return;
        }

        // 1- Query existing documents by normalizedUrl
        Set<String> allUrls = crawledDocs.keySet();
        Query query = new Query(Criteria.where("normalizedUrl").in(allUrls));
        query.fields().include("normalizedUrl");
        List<UrlDocument> existingDocs = mongoOperations.find(query, UrlDocument.class, "urlsfrontier");
        Set<String> existingUrlSet = new HashSet<>(
            existingDocs.stream().map(UrlDocument::getNormalizedUrl).toList()
        );

        // 2- Create bulk operations
        BulkOperations bulkOps = mongoOperations.bulkOps(BulkMode.UNORDERED, UrlDocument.class, "urlsfrontier");

        // 3- Insert or update the documents
        for (Map.Entry<String, UrlDocument> entry : crawledDocs.entrySet()) {
            String url = entry.getKey();
            UrlDocument doc = entry.getValue();

            if (existingUrlSet.contains(url)) {
                // Update existing document
                Query updateQuery = Query.query(Criteria.where("normalizedUrl").is(url));
                Update update = new Update()
                    .set("document", doc.getDocument())
                    .set("hashedDocContent", doc.getHashedDocContent())
                    .set("linkedPages", doc.getLinkedPages())
                    .set("isCrawled", doc.isCrawled())
                    .set("lastCrawled", doc.getLastCrawled());
                bulkOps.updateOne(updateQuery, update);
            } else {
                // Insert new document
                // We need to set all fields since it's a new document
                UrlDocument newDoc = new UrlDocument(
                    url,
                    doc.getFrequency(),
                    doc.isCrawled(),
                    null,
                    doc.getHashedDocContent(),
                    doc.getLinkedPages(),
                    doc.getLastCrawled()
                );
                newDoc.setDocument(doc.getDocument());
                bulkOps.insert(newDoc);
            }
        }

        // 4- Execute bulk operation
        try {
            BulkWriteResult result = bulkOps.execute();
            System.out.println(
                "Inserted: " + result.getInsertedCount() +
                ", Updated: " + result.getModifiedCount()
            );
        } catch (Exception e) {
            System.err.println("Error during bulk save: " + e.getMessage());
        }
    }

    public int count() {
        return (int) urlsFrontierRepository.count();
    }

    public List<UrlDocument> saveAll(List<UrlDocument> urls) {
        return urlsFrontierRepository.saveAll(urls);
    }

    public void deleteByNormalizedUrl(String normalizedUrl) {
        urlsFrontierRepository.deleteByNormalizedUrl(normalizedUrl);
    }

    public List<String> findAllHashedDocContent() {
        List<String> allHash = urlsFrontierRepository.findAllHashedDocContent();
        return JsonParserUtil.parseSingleField(allHash, "hashedDocContent");
    }
}