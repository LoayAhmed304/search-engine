package com.project.searchengine.server.service;

import com.project.searchengine.server.model.InvertedIndex;
import com.project.searchengine.server.model.PageReference;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    @Autowired
    private InvertedIndexService invertedIndexService;

    @Autowired
    public QueryService(InvertedIndexService invertedIndexService) {
        this.invertedIndexService = invertedIndexService;
    }

    public List<PageReference> getTokenPages(String token) {
        InvertedIndex invertedIndex = invertedIndexService.getInvertedIndex(token);
        return invertedIndex != null ? invertedIndex.getPages() : Collections.emptyList();
    }
}
