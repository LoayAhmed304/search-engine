package com.project.repository;
import org.springframework.data.mongodb.repository.*;
import org.springframework.stereotype.Repository;
import com.project.model.Page;

public class PageRepository extends MongoRepository<Page, String> {

} 
