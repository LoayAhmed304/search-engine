package com.project.searchengine.indexer;

import com.project.searchengine.crawler.preprocessing.*;
import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.service.PageService;
import java.security.MessageDigest;
import java.util.*;
import javax.xml.bind.DatatypeConverter;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.*;

@Service
public class Indexer {

    @Autowired
    private Tokenizer tokenizer;

    @Autowired
    private PageService pageService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Preprocesses the document by extracting tokens and saving the page.
     * @param url The URL of the document.
     * @param document The Jsoup Document object.
     */
    public void preprocessDocument(String url, Document document) {
        // Extract raw text
        String title = document.title();
        String id = hashUrl(url);
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6, title");

        // Tokenize the document
        long start = System.nanoTime();
        tokenizer.tokenizeContent(content, id, "body");
        tokenizer.tokenizeHeaders(fieldTags, id);
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Tokenization took: " + duration + " ms");
        saveTokens();

        savePage(id, url, title, content);
    }

    /**
     * Saves the page to the database.
     * @param id The unique identifier for the page.
     * @param url The URL of the page.
     * @param title The title of the page.
     * @param content The content of the page.
     */
    public void savePage(String id, String url, String title, String content) {
        Page page = new Page(id, url, title, content);

        pageService.createPage(page);
    }

    /**
     * Save tokens in the index buffer to the database
     */
    public void saveTokens() {
        Map<String, InvertedIndex> indexBuffer = tokenizer.getIndexBuffer();
        System.out.println("Tokens size: " + indexBuffer.size());

        if (!indexBuffer.isEmpty()) {
            long start = System.nanoTime();
            BulkOperations bulkOps = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                InvertedIndex.class
            );

            for (InvertedIndex index : indexBuffer.values()) {
                Query query = new Query(Criteria.where("word").is(index.getWord()));

                for (PageReference newPage : index.getPages()) {
                    // Add the new page to the existing pages
                    Update update = new Update()
                        .addToSet("pages", newPage)
                        .inc("pageCount", index.getPageCount());
                    bulkOps.upsert(query, update);
                }
            }
            bulkOps.execute();
            long duration = (System.nanoTime() - start) / 1_000_000;
            System.out.println("saving to the database took: " + duration + " ms");
            indexBuffer.clear();
        }
    }

    /**
     * Hash the url to create a unique id using sha-256
     * @param url The url to be hashed
     * @return The hashed url as a string
     */
    private String hashUrl(String url) {
        // Normalize the url first
        String normalizedUrl = URLNormalizer.normalizeUrl(url);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedUrl.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (Exception e) {
            return Integer.toHexString(url.hashCode());
        }
    }
}
