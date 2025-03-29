package com.project.searchengine.server.repository.*;
import com.project.searchengine.server.model.Page;


public interface PageRepository extends MongoRepository<Page, String> {
    // @Query("{ 'url' : ?0 }")
    // Page findByUrl(String url);

    // void createPage(Page page);

}
