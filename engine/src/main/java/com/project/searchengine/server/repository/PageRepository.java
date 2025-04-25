package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PageRepository extends MongoRepository<Page, String> {
    /**
     * Check if a page already exists in the database
     *
     */
    public boolean existsById(String id);

    public Page getPageById(String id);
}
