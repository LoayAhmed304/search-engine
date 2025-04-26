package com.project.searchengine.server.service;

import com.mongodb.bulk.*;
import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.repository.PageRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.*;
import org.springframework.stereotype.Service;

@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Gets total number of documents in the Page database
     *
     * @return number of entries in Page collection
     */
    public long getTotalDocuments() {
        return pageRepository.count();
    }

    public List<Page> getAllPages() {
        return pageRepository.findAll();
    }

    public Page createPage(Page page) {
        return pageRepository.save(page);
    }

    public List<Page> saveAll(List<Page> pages) {
        return pageRepository.saveAll(pages);
    }

    /**
     * Check if a page already exists in the database
     *
     * @param pageId Id of the page
     * @return page exists or not
     */
    public boolean existsById(String pageId) {
        return pageRepository.existsById(pageId);
    }

    public Page getPage(String id) {
        return pageRepository.getPageById(id);
    }

    /**
     * Saves a list of pages in bulk to the database.
     *
     * @param pages List of Page objects to be saved
     */
    public void savePagesInBulk(List<Page> pages) {
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
