package com.project.searchengine.indexer;

import com.project.searchengine.ranker.RankCalculator;
import com.project.searchengine.server.model.*;
import com.project.searchengine.server.service.*;
import com.project.searchengine.utils.*;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Indexer {

    @Autowired
    private Tokenizer tokenizer;

    @Autowired
    private PageService pageService;

    @Autowired
    private InvertedIndexService invertedIndexService;

    @Autowired
    private UrlsFrontierService urlsFrontierService;

    public static int BATCH_SIZE = 50;
    public static int currentBatch = 1;

    /**
     * Starts the indexing process by fetching documents from the database and indexing them in batches.
     * It continues until there are no more documents to index.
     *
     * This method is called by the main application to initiate the indexing process.
     */
    public void startIndexing() {
        System.out.println("Starting indexing process...");

        while (true) {
            // Get a batch of non indexed documents from the database
            List<UrlDocument> urlDocuments = urlsFrontierService.getNotIndexedDocuments(BATCH_SIZE);

            // If there are no more documents to index, break the loop
            if (urlDocuments.isEmpty()) {
                System.out.println("No more documents to index");
                return;
            }

            // Index all batches
            indexBatch(urlDocuments);
            currentBatch++;
        }
    }

    /**
     * Indexes a batch of URL documents with a certain size.
     *
     * @param urlDocuments The list of URL documents to be indexed.
     */
    public void indexBatch(List<UrlDocument> urlDocuments) {
        long start = System.nanoTime();

        List<UrlDocument> updatedUrlDocuments = new ArrayList<>();
        List<Page> savedPages = new ArrayList<>();

        for (UrlDocument urlDocument : urlDocuments) {
            // Index each document in the batch
            indexDocument(urlDocument, updatedUrlDocuments, savedPages);
        }

        // Compute the term frequency (TF) for the tokens
        Map<String, InvertedIndex> indexBuffer = tokenizer.getIndexBuffer();
        Map<String, Integer> pagesTokensCount = tokenizer.getPagesTokensCount();
        RankCalculator.calculateTf(indexBuffer, pagesTokensCount);

        // Save the tokens, updated URL documents and pages to the database
        saveToDatabase(updatedUrlDocuments, savedPages, indexBuffer);

        // Reset tokenizer for the next batch
        tokenizer.resetForNewBatch();

        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(
            "Indexing Batch " +
            currentBatch +
            " took: " +
            duration +
            " ms, processed " +
            urlDocuments.size() +
            " documents"
        );
    }

    /**
     * Indexes a single Url document by parsing the jsoup document, extracting its content and headers,
     * and tokenizing them.
     *
     * @param urlDocument The URL document to be indexed.
     * @param updatedUrlDocuments The list of URL documents to be updated.
     * @param savedPages The list of pages to be saved.
     */
    public void indexDocument(
        UrlDocument urlDocument,
        List<UrlDocument> updatedUrlDocuments,
        List<Page> savedPages
    ) {
        long start = System.nanoTime();
        // Get the document from the database
        String url = urlDocument.getNormalizedUrl();
        String document = CompressionUtil.decompress(urlDocument.getDocument());

        // Check null documents
        if (document == null) {
            System.out.println("Skipping null document for URL:" + url);
            urlDocument.setIndexed(true);
            updatedUrlDocuments.add(urlDocument);
            return;
        }

        // Convert the document to a Jsoup Document object
        Document jsoupDocument = Jsoup.parse(document);

        // Call the index method with the URL and the Jsoup Document object
        index(url, jsoupDocument);

        // Set the page token count in the page object
        // Check if the page already exists in the database
        String pageId = HashManager.hash(url);
        int pageTokenCount;
        if (!pageService.existsById(pageId)) {
            pageTokenCount = tokenizer.getPageTokenCount(pageId);
            savedPages.add(
                new Page(
                    pageId,
                    url,
                    jsoupDocument.title(),
                    jsoupDocument.body().text().toLowerCase(),
                    pageTokenCount
                )
            );
        } else {
            System.out.println("Page already exists for URL: " + url + ", skipping save.");
            urlDocument.setIndexed(true);
            updatedUrlDocuments.add(urlDocument);
            return;
        }

        // Add the document to the updatedUrlDocuments list
        urlDocument.setIndexed(true);
        updatedUrlDocuments.add(urlDocument);
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(
            "Indexing document took: " +
            duration +
            " ms, processed URL: " +
            url +
            ", pageId: " +
            pageId
        );
    }

    /**
     * Processes a single document by extracting its content and headers, and tokenizing them.
     *
     * @param url The URL of the document.
     * @param document The Jsoup Document object.
     */
    public void index(String url, Document document) {
        // Extract raw text
        String id = HashManager.hash(url);
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, title");

        tokenizer.tokenizeContent(content, id);
        tokenizer.tokenizeHeaders(fieldTags, id);
    }

    /**
     * Saves the tokens, updated URL documents and pages to the database.
     *
     * @param updatedUrlDocuments The list of URL documents to be updated.
     * @param savedPages The list of pages to be saved.
     */
    public void saveToDatabase(
        List<UrlDocument> updatedUrlDocuments,
        List<Page> savedPages,
        Map<String, InvertedIndex> indexBuffer
    ) {
        long start = System.nanoTime();
        // Save the pages in bulk
        pageService.savePagesInBulk(savedPages);
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(
            "Saving pages took: " + duration + " ms, saved " + savedPages.size() + " pages"
        );

        // Save the inverted index in bulk
        start = System.nanoTime();
        invertedIndexService.saveTokensInBulk(tokenizer.getIndexBuffer());
        duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(
            "Saving tokens took: " + duration + " ms, saved " + indexBuffer.size() + " tokens"
        );

        // Save the updated URL documents in bulk
        start = System.nanoTime();
        urlsFrontierService.updateUrlDocumentsInBulk(updatedUrlDocuments);
        duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(
            "Saving URL documents took: " +
            duration +
            " ms, updated " +
            updatedUrlDocuments.size() +
            " documents"
        );
    }
}
