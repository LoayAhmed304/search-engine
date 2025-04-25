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
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

@Service
public class UrlsFrontierService {

    private final UrlsFrontierRepository urlsFrontierRepository;
    private final MongoOperations mongoOperations;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public UrlsFrontierService(UrlsFrontierRepository urlsFrontierRepository, MongoTemplate mongoTemplate) {
        this.urlsFrontierRepository = urlsFrontierRepository;
        this.mongoOperations = mongoTemplate;
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

   
    public void bulkSaveCrawledBatch(Map<String, UrlDocument> crawledDocs) {
        if (crawledDocs.isEmpty()) return;
    
        // 1. Lightweight existence check (URLs only)
        Set<String> allUrls = crawledDocs.keySet();
        Query query = new Query(Criteria.where("normalizedUrl").in(allUrls));
        query.fields()
            .include("normalizedUrl")
            .exclude("_id");  // Fixed syntax for field projection
    
        Set<String> existingUrls = new HashSet<>(
            mongoOperations.find(query, UrlDocument.class, "urlsfrontier")
                .stream()
                .map(UrlDocument::getNormalizedUrl)
                .toList()
        );
    
        // 2. Prepare bulk ops
        BulkOperations bulkOps = mongoOperations.bulkOps(
            BulkMode.UNORDERED, 
            UrlDocument.class, 
            "urlsfrontier"
        );
    
        // 3. Build operations
        crawledDocs.forEach((url, doc) -> {
            Query updateQuery = Query.query(Criteria.where("normalizedUrl").is(url));
            Update update = new Update()
                .set("document", doc.getDocument())
                .set("hashedDocContent", doc.getHashedDocContent())
                .set("linkedPages", doc.getLinkedPages())
                .set("isCrawled", doc.isCrawled())
                .set("lastCrawled", doc.getLastCrawled());
    
            if (existingUrls.contains(url)) {
                bulkOps.updateOne(updateQuery, update);
            } else {
                bulkOps.insert(doc);  // Insert the complete document
            }
        });
    
        // 4. Execute
        try {
            BulkWriteResult result = bulkOps.execute();
            System.out.printf("Bulk save: %d inserts, %d updates\n",
                result.getInsertedCount(),
                result.getModifiedCount());
        } catch (Exception e) {
            System.err.println("Bulk save failed: " + e.getMessage());
        }
    }

    public int count() {
        return (int) urlsFrontierRepository.count();
    }

    public List<UrlDocument> saveAll(List<UrlDocument> urls) {
        return urlsFrontierRepository.saveAll(urls);
    }

    /**
     * Get a list of URL documents that are not indexed yet.
     * @param limit The maximum number of documents to retrieve
     *
     * @return A list of URL documents that are not indexed yet
     */
    public List<UrlDocument> getNotIndexedDocuments(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return urlsFrontierRepository.findByIsIndexedFalse(pageable).getContent();
    }

    /**
     * Deletes a document with the given normalizedUrl from the database.
     *
     * @param normalizedUrl The normalized URL of the document to delete
     */
    public void deleteByNormalizedUrl(String normalizedUrl) {
        urlsFrontierRepository.deleteByNormalizedUrl(normalizedUrl);
    }

    public List<String> findAllHashedDocContent() {
        List<String> allHash = urlsFrontierRepository.findAllHashedDocContent();
        return JsonParserUtil.parseSingleField(allHash, "hashedDocContent");
    }

    /**
     * Bulk Update the isIndexed field of multiple URL documents to true.
     *
     * @param documents List of URL documents to update
     */
    public void updateUrlDocumentsInBulk(List<UrlDocument> documents) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED,
            UrlDocument.class
        );

        for (UrlDocument document : documents) {
            Query query = new Query(Criteria.where("_id").is(document.getId()));
            Update update = new Update().set("isIndexed", true);
            bulkOps.updateOne(query, update);
        }

        try {
            bulkOps.execute();
        } catch (Exception e) {
            System.err.println("Error updating URL documents: " + e.getMessage());
        }
    }
}