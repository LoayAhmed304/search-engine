package com.project.searchengine.server.service;

import com.project.searchengine.server.model.SearchQuery;
import com.project.searchengine.server.repository.SearchQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchQueryService {

    @Autowired
    private SearchQueryRepository searchQueryRepository;

    public SearchQueryService(SearchQueryRepository searchQueryRepository) {
        this.searchQueryRepository = searchQueryRepository;
    }

    public void saveSearchQuery(SearchQuery query) {
        // Save the search query to the database
        searchQueryRepository.save(query);
    }
}
