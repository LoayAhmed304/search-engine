package com.project.searchengine.server.service;

import com.project.searchengine.server.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class PageReferenceService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public double getPageRank(String pageId) {
        Query query = Query.query(Criteria.where("_id").is(pageId));
        query.fields().include("rank");

        Page page = mongoTemplate.findOne(query, Page.class);
        if (page == null) {
            throw new IllegalArgumentException("Page not found with ID: " + pageId);
        }
        return page.getRank();
    }
}
