package com.project.searchengine.indexer;

import com.project.searchengine.server.model.*;
import com.project.searchengine.server.service.*;
import com.project.searchengine.utils.HashManager;
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

    public static int BATCH_SIZE = 100;
    public static int currentBatch = 1;

    public void startIndexing() {
        System.out.println("Starting indexing process...");

        while (true) {
            // Get a batch of not indexed documents from the database
            List<UrlDocument> urlDocuments = urlsFrontierService.getNotIndexedDocuments(BATCH_SIZE);

            if (urlDocuments.isEmpty()) {
                System.out.println("No more documents to index");
                return;
            }

            // Index all batches
            indexBatch(urlDocuments);
            currentBatch++;
        }
    }

    public void indexBatch(List<UrlDocument> urlDocuments) {
        long start = System.nanoTime();

        List<UrlDocument> updatedUrlDocuments = new ArrayList<>();
        List<Page> savedPages = new ArrayList<>();

        for (UrlDocument urlDocument : urlDocuments) {
            // 1- Get the document from the database
            String url = urlDocument.getNormalizedUrl();
            String document = urlDocument.getDocument();

            // 2- Convert the document to a Jsoup Document object
            Document jsoupDocument = Jsoup.parse(document);

            // 3- Call the index method with the URL and the Jsoup Document object
            indexDocument(url, jsoupDocument);

            // 4- Set the page token count
            tokenizer.setPageTokenCount();

            // 5- Add the page to the pages list to bulk save it
            savedPages.add(new Page(HashManager.hash(url), url, jsoupDocument.title(), document));

            // 6- Add the document to the updatedUrlDocuments list
            urlDocument.setIndexed(true);
            updatedUrlDocuments.add(urlDocument);
        }

        // Bulk save tokens, update URL documents, and pages in the database
        Map<String, InvertedIndex> indexBuffer = tokenizer.getIndexBuffer();
        invertedIndexService.saveTokensInBulk(indexBuffer);
        pageService.savePagesInBulk(savedPages);
        urlsFrontierService.updateUrlDocumentsInBulk(updatedUrlDocuments);

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
     * Preprocesses the document by extracting tokens and saving the page.
     * @param url The URL of the document.
     * @param document The Jsoup Document object.
     */
    public void indexDocument(String url, Document document) {
        // Extract raw text
        String id = HashManager.hash(url);
        String content = document.body().text();
        Elements fieldTags = document.select("h1, h2, h3, h4, h5, h6, title");

        tokenizer.tokenizeContent(content, id, "body");
        tokenizer.tokenizeHeaders(fieldTags, id);
    }
}
