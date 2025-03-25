package com.project.SearchEngine.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.SearchEngine.server.model.Page;


public interface PageRepository extends MongoRepository<Page, String> {

    // @Query("{ 'url' : ?0 }")
    // Page findByUrl(String url);


    // void createPage(Page page);

} 
