package com.project.SearchEngine.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.SearchEngine.server.model.Page;


public interface PageRepository extends MongoRepository<Page, String> {

    // @Query("{ 'url' : ?0 }")
    // Page findByUrl(String url);


    // void createPage(Page page);

} 
