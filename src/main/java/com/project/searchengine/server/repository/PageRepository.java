package com.project.searchengine.server.repository;

import com.project.searchengine.server.model.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PageRepository extends MongoRepository<Page, String> {
    // @Query("{ 'url' : ?0 }")
    // Page findByUrl(String url);

    // void createPage(Page page);

}
