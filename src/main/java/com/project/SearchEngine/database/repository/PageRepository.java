package com.project.SearchEngine.database.repository;

import com.project.SearchEngine.database.model.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


public interface PageRepository extends MongoRepository<Page, String> {
    Page findByUrl(String url);
} 
