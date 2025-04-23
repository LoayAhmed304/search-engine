package com.project.searchengine.indexer;

import com.mongodb.bulk.BulkWriteResult;
import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.model.UrlDocument;
import com.project.searchengine.server.repository.InvertedIndexRepository;
import com.project.searchengine.server.service.PageService;
import com.project.searchengine.server.service.UrlsFrontierService;
import com.project.searchengine.utils.HashManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class Indexer {

    @Autowired
    private Tokenizer tokenizer;

    @Autowired
    private PageService pageService;

    @Autowired
    private UrlsFrontierService urlsFrontierService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    public static int BATCH_SIZE = 1000;

    public void startIndexing() {
        System.out.println("Starting indexing process...");
        long start = System.nanoTime();

        // Get a batch of not indexed documents from the database
        List<UrlDocument> urlDocuments = urlsFrontierService.getNotIndexedDocuments(BATCH_SIZE);
        List<UrlDocument> updatedUrlDocuments = new ArrayList<>();
        List<Page> savedPages = new ArrayList<>();

        if (urlDocuments.isEmpty()) {
            System.out.println("No documents to index");
            return;
        }

        for (UrlDocument urlDocument : urlDocuments) {
            // 1- Get the document from the database
            String url = urlDocument.getNormalizedUrl();
            String document = urlDocument.getDocument();

            // 2- Convert the document to a Jsoup Document object
            Document jsoupDocument = Jsoup.parse(document);

            // 3- Call the index method with the URL and the Jsoup Document object
            index(url, jsoupDocument);

            // 4- Set the page token count
            tokenizer.setPageTokenCount();

            // 6- Save the page to the database
            savedPages.add(new Page(HashManager.hash(url), url, jsoupDocument.title(), document));
            // 7- Update the URL document in the database
            urlDocument.setIndexed(true);
            updatedUrlDocuments.add(urlDocument);
        }

        saveTokens();
        updateUrlDocuments(updatedUrlDocuments);
        savePages(savedPages);

        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(
            "Indexing batch took: " +
            duration +
            " ms, processed " +
            urlDocuments.size() +
            " documents"
        );
    }

    /**
     * Preprocesses the document by extracting tokens and saving the page.
     * @param url The URL of the document.
     * @param document The Jsoup Document object.
     */
    public void index(String url, Document document) {
        // Extract raw text
        String id = HashManager.hash(url);
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6, title");

        tokenizer.tokenizeContent(content, id, "body");
        tokenizer.tokenizeHeaders(fieldTags, id);
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
                    result.getModifiedCount()
                );
            } catch (Exception e) {
                System.err.println("Error saving tokens: " + e.getMessage());
            }
            indexBuffer.clear();
        }
    }

    public void updateUrlDocuments(List<UrlDocument> documents) {
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

    public void savePages(List<Page> pages) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED,
            Page.class
        );

        // Insert pages
        for (Page page : pages) {
            bulkOps.insert(page);
        }

        try {
            BulkWriteResult result = bulkOps.execute();
            System.out.println("Inserted Pages: " + result.getInsertedCount());
        } catch (Exception e) {
            System.err.println("Error saving pages: " + e.getMessage());
        }
    }
}
