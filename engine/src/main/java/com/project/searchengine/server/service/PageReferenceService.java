package com.project.searchengine.server.service;

import com.project.searchengine.server.model.Page;
import com.project.searchengine.server.model.PageReference;
import com.project.searchengine.server.repository.PageRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class PageReferenceService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PageRepository pageRepository;

    public Map<String, Double> getPagesRanks(List<String> pageIds) {
        Query query = Query.query(Criteria.where("_id").in(pageIds));
        query.fields().include("rank");

        List<Page> pages = mongoTemplate.find(query, Page.class);

        return pages.stream().collect(Collectors.toMap(Page::getId, Page::getRank, (a, b) -> a));
    }

    public String getPageBodyContent(PageReference referencePage) {
        String pageId = referencePage.getPageId();
        Page page = pageRepository.getPageById(pageId);

        String content = page.getContent();

        return content.toLowerCase();
    }
}
